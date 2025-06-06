const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

const cloudinary = require("cloudinary").v2;
const BusBoy = require("busboy"); // Sửa lại tên biến cho đúng chuẩn (viết hoa chữ cái đầu)
const os = require("os");
const fs = require("fs");
const path = require("path");

// Lấy credentials từ biến môi trường đã set
const CLOUD_NAME = functions.config().cloudinary.cloud_name;
const API_KEY = functions.config().cloudinary.api_key;
const API_SECRET = functions.config().cloudinary.api_secret;

// Kiểm tra xem credentials có tồn tại không trước khi cấu hình Cloudinary
if (!CLOUD_NAME || !API_KEY || !API_SECRET) {
  console.error(
    "Cloudinary configuration missing. Ensure 'cloudinary.cloud_name', 'cloudinary.api_key', and 'cloudinary.api_secret' are set in Firebase functions config.",
  );
} else {
  cloudinary.config({
    cloud_name: CLOUD_NAME,
    api_key: API_KEY,
    api_secret: API_SECRET,
    secure: true, // Luôn sử dụng HTTPS cho URL trả về
  });
  console.log("Cloudinary configured successfully.");
}

exports.uploadImageToCloudinary = functions
  // Chọn region gần với người dùng/server Cloudinary của bạn nhất
  // Ví dụ: 'asia-southeast1' (Singapore), 'us-central1', 'europe-west1'
  .region("asia-southeast1") // Thay đổi nếu cần
  .https.onRequest(async (req, res) => {
    // --- CORS Headers ---
    // Cho phép tất cả các domain trong quá trình phát triển.
    // **QUAN TRỌNG**: Trong production, hãy giới hạn lại domain cụ thể của bạn!
    // Hoặc sử dụng Firebase App Check để bảo vệ function.
    res.set("Access-Control-Allow-Origin", "*");
    res.set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
    res.set(
      "Access-Control-Allow-Headers",
      "Content-Type, Authorization",
    );

    if (req.method === "OPTIONS") {
      // Pre-flight request (trình duyệt gửi trước khi gửi POST thực sự với CORS)
      console.log("Handling OPTIONS pre-flight request.");
      res.status(204).send("");
      return;
    }

    if (req.method !== "POST") {
      console.log(`Method not allowed: ${req.method}`);
      res
        .status(405)
        .json({error: "Method not allowed. Please use POST."});
      return;
    }

    // --- Xác thực người dùng (Quan trọng) ---
    let idToken;
    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer ')) {
        idToken = req.headers.authorization.split('Bearer ')[1];
    }
    let decodedToken;
    if (idToken) {
      try {
        decodedToken = await admin.auth().verifyIdToken(idToken);
        console.log(`User authenticated: ${decodedToken.uid}`);
      } catch (error) {
        console.error("Error verifying Firebase ID token:", error);
        res
          .status(403)
          .json({error: "Unauthorized: Invalid or expired token."});
        return;
      }
    } else {
      console.warn("No ID token provided.");
      res.status(403).json({error: "Unauthorized: No token provided."});
      return;
    }
    const userId = decodedToken.uid;

    // --- Xử lý file upload với BusBoy ---
    if (!CLOUD_NAME || !API_KEY || !API_SECRET) {
        console.error("Cloudinary is not configured due to missing credentials. Aborting upload.");
        res.status(500).json({ error: "Server configuration error for image processing." });
        return;
    }

    const busboy = BusBoy({headers: req.headers});
    const tmpdir = os.tmpdir();
    const uploads = {};
    const fileWrites = [];

    busboy.on("file", (fieldname, fileStream, fileInfo) => {
      const {filename, encoding, mimeType} = fileInfo;
      console.log(
        `Processing file [${fieldname}]: filename: ${filename}, encoding: ${encoding}, mimetype: ${mimeType}`,
      );

      if (!filename) {
        console.warn(
          `File stream for field [${fieldname}] has no filename. Skipping.`,
        );
        fileStream.resume(); // Quan trọng: tiêu thụ stream để busboy tiếp tục
        return;
      }

      // Kiểm tra loại file (chỉ cho phép ảnh)
      if (!mimeType || !mimeType.startsWith("image/")) {
        console.warn(
          `Invalid file type received: ${mimeType}. Skipping file: ${filename}`,
        );
        fileStream.resume(); // Tiêu thụ stream
        // Client nên kiểm tra loại file trước khi gửi, nhưng server cũng nên kiểm tra
        // Bạn có thể chọn trả lỗi ở đây nếu muốn chặt chẽ hơn:
        // req.unpipe(busboy); // Ngừng parse
        // res.status(400).json({ error: `Invalid file type: ${mimeType}. Only images are allowed.` });
        // return; // Hoặc set một flag để báo lỗi ở 'finish'
        return;
      }

      const uniqueFilename = `${path.parse(filename).name}_${Date.now()}${path.parse(filename).ext}`;
      const filepath = path.join(tmpdir, uniqueFilename);
      uploads[fieldname] = {filepath, mimetype, originalFilename: filename};

      console.log(`Saving temporary file to: ${filepath}`);
      const writeStream = fs.createWriteStream(filepath);
      fileStream.pipe(writeStream);

      const promise = new Promise((resolve, reject) => {
        fileStream.on("end", () => {
          console.log(`File stream ended for ${filename}`);
          writeStream.end();
        });
        writeStream.on("finish", () => {
          console.log(`File ${filename} finished writing to ${filepath}`);
          resolve({fieldname, filepath});
        });
        writeStream.on("error", (err) => {
          console.error(`Error writing file ${filename} to temp:`, err);
          reject(err);
        });
      });
      fileWrites.push(promise);
    });

    busboy.on("field", (fieldname, val) => {
      console.log(`Processed field ${fieldname}: ${val}.`);
    });

    busboy.on("finish", async () => {
      console.log("Busboy finished parsing form.");
      if (fileWrites.length === 0) {
        console.warn("No files were processed or written to temp storage.");
        // Kiểm tra xem có uploads nào được thêm vào không (trường hợp file type không hợp lệ)
        if (Object.keys(uploads).length === 0) {
           res.status(400).json({ error: "No valid image file uploaded or file type was incorrect." });
           return;
        }
      }

      try {
        await Promise.all(fileWrites);
        console.log("All temporary files written successfully.");
      } catch (err) {
        console.error(
          "Error writing one or more files to temp storage:",
          err,
        );
        res
          .status(500)
          .json({error: "Failed to process file uploads.", details: err.message});
        return;
      }

      const fileDataField = "imageFile"; // Tên field mà client sẽ gửi file
      const fileData = uploads[fileDataField];

      if (!fileData || !fileData.filepath || !fs.existsSync(fileData.filepath)) {
        console.warn(
          `No valid file uploaded with fieldname "${fileDataField}" or temp file path is missing/invalid: ${fileData && fileData.filepath ? fileData.filepath : 'undefined'}`,
        );
        res.status(400).json({
          error:
            `No file uploaded with fieldname "${fileDataField}" or file processing failed.`,
        });
        return;
      }

      try {
        console.log(
          `Attempting to upload ${fileData.filepath} to Cloudinary...`,
        );
        const result = await cloudinary.uploader.upload(fileData.filepath, {
          folder: `tradeup_app/${userId}`, // Tổ chức file trên Cloudinary
          public_id: `${path.parse(fileData.originalFilename).name}_${Date.now()}`, // Tạo public_id duy nhất
          resource_type: "auto", // Cloudinary tự động nhận diện loại file
          // Ví dụ thêm eager transformation để tạo thumbnail:
          // eager: [
          //   { width: 400, crop: "limit" }, // Resize giữ tỉ lệ, max width 400
          //   { width: 150, height: 150, crop: "fill", gravity: "face" } // Thumbnail 150x150
          // ]
        });

        fs.unlink(fileData.filepath, (unlinkErr) => { // Xóa file tạm (bất đồng bộ)
          if (unlinkErr) console.error(`Error deleting temp file ${fileData.filepath}:`, unlinkErr);
          else console.log(`Temp file ${fileData.filepath} deleted.`);
        });

        console.log("Cloudinary Upload Successful:", result.secure_url);
        res.status(200).json({
          message: "File uploaded successfully to Cloudinary!",
          imageUrl: result.secure_url,
          publicId: result.public_id,
          // Nếu có eager transformations, URL của chúng nằm trong result.eager
          // eagerThumbnails: result.eager ? result.eager.map(t => t.secure_url) : []
        });
      } catch (uploadError) {
        console.error("Error uploading to Cloudinary:", uploadError);
        if (fs.existsSync(fileData.filepath)) {
          fs.unlink(fileData.filepath, (unlinkErr) => {
            if (unlinkErr) console.error(`Error deleting temp file ${fileData.filepath} after upload error:`, unlinkErr);
          });
        }
        let errorMessage = "Failed to upload image to Cloudinary.";
        if (uploadError.error && uploadError.error.message) {
          errorMessage += ` Details: ${uploadError.error.message}`;
        } else if (uploadError.message) {
          errorMessage += ` Details: ${uploadError.message}`;
        }
        res.status(500).json({error: errorMessage, details: uploadError});
      }
    });

    busboy.on("error", (err) => {
      console.error("Busboy error:", err);
      res
        .status(500)
        .json({error: "File processing error on server.", details: err.message});
    });

    // Bắt đầu pipe request vào busboy
    if (req.rawBody) {
      console.log("Processing request with rawBody.");
      busboy.end(req.rawBody);
    } else {
      console.log("Piping request to Busboy.");
      req.pipe(busboy);
    }
  });