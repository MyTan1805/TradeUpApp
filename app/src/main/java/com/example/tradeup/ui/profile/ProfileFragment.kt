package com.example.tradeup.ui.profile

// import androidx.recyclerview.widget.GridLayoutManager // Sẽ dùng khi có Adapter
// import com.example.tradeup.ui.adapters.UserItemListAdapter // << SẼ CẦN ADAPTER
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tradeup.R
import com.example.tradeup.data.model.User
import com.example.tradeup.databinding.FragmentProfileBinding
import com.example.tradeup.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels() // Để dùng cho logout

    // private lateinit var userItemListAdapter: UserItemListAdapter // << SẼ CẦN ADAPTER

    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView called")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called. Profile User ID Arg: ${profileViewModel.profileUserIdArg}, Current Auth UID: ${profileViewModel.currentAuthUserUid}")

        setupToolbar()
        // setupRecyclerView() // << SẼ CẦN KHI CÓ ADAPTER
        setupObservers()
        setupClickListeners()

        // profileViewModel.loadUserProfile() // ViewModel đã tự load trong init
    }

    private fun setupToolbar() {
        binding.toolbarProfile.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.profile_toolbar_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                // Ẩn/hiện item menu dựa trên việc đây là profile của ai
                val state = profileViewModel.profileState.value
                if (state is ProfileState.Success) {
                    menu.findItem(R.id.action_edit_profile)?.isVisible = state.isCurrentUserProfile
                    menu.findItem(R.id.action_settings)?.isVisible = state.isCurrentUserProfile
                    menu.findItem(R.id.action_logout)?.isVisible = state.isCurrentUserProfile
                    menu.findItem(R.id.action_report_user)?.isVisible = !state.isCurrentUserProfile
                } else { // Đang load hoặc lỗi, ẩn hết các item tùy chọn
                    menu.findItem(R.id.action_edit_profile)?.isVisible = false
                    menu.findItem(R.id.action_settings)?.isVisible = false
                    menu.findItem(R.id.action_logout)?.isVisible = false
                    menu.findItem(R.id.action_report_user)?.isVisible = false
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit_profile -> {
                        Log.d(TAG, "Edit Profile clicked")
                        // TODO: Navigate to EditProfileFragment
                        // findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
                        Toast.makeText(requireContext(), "Chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_settings -> {
                        Log.d(TAG, "Settings clicked")
                        // TODO: Navigate to SettingsFragment
                        Toast.makeText(requireContext(), "Cài đặt", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_report_user -> {
                        Log.d(TAG, "Report User clicked")
                        // TODO: Show report user dialog/fragment
                        Toast.makeText(requireContext(), "Báo cáo người dùng", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_logout -> {
                        Log.d(TAG, "Logout clicked")
                        authViewModel.logoutUser() // Sử dụng AuthViewModel để logout
                        // Điều hướng về màn hình đăng nhập/màn hình chính (sau khi logout thành công)
                        // sẽ được xử lý bởi observer của AuthViewModel hoặc một cơ chế điều hướng chung
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /* // Sẽ bật lại khi có Adapter
    private fun setupRecyclerView() {
        userItemListAdapter = UserItemListAdapter { item ->
            // TODO: Handle item click, navigate to item detail
            Log.d(TAG, "Clicked on item: ${item.title}")
            // val action = ProfileFragmentDirections.actionProfileFragmentToItemDetailFragment(item.id)
            // findNavController().navigate(action)
        }
        binding.recyclerViewUserListings.apply {
            adapter = userItemListAdapter
            layoutManager = GridLayoutManager(requireContext(), 2) // Ví dụ 2 cột
            // addItemDecoration(...) // Nếu cần
        }
    }
    */

    private fun setupClickListeners() {
        // Nút chỉnh sửa profile (nếu không dùng menu item)
        binding.buttonActionTopRight.setOnClickListener {
            if (profileViewModel.profileState.value is ProfileState.Success &&
                (profileViewModel.profileState.value as ProfileState.Success).isCurrentUserProfile) {
                Log.d(TAG, "Edit Profile button (top right) clicked")
                // TODO: Navigate to EditProfileFragment
                Toast.makeText(requireContext(), "Chỉnh sửa hồ sơ (button)", Toast.LENGTH_SHORT).show()
            }
        }

        // Nút nhắn tin
        binding.buttonSendMessage.setOnClickListener {
            val state = profileViewModel.profileState.value
            if (state is ProfileState.Success && !state.isCurrentUserProfile) {
                Log.d(TAG, "Send Message button clicked to user: ${state.user.uid}")
                // TODO: Navigate to ChatFragment with state.user.uid
                // val action = ProfileFragmentDirections.actionProfileFragmentToChatFragment(state.user.uid)
                // findNavController().navigate(action)
                Toast.makeText(requireContext(), "Nhắn tin tới ${state.user.displayName}", Toast.LENGTH_SHORT).show()
            }
        }

        // Các link trong layoutMyProfileActions
        binding.textViewMyListingsLink.setOnClickListener {
            Log.d(TAG, "My Listings link clicked")
            // TODO: Navigate to user's listings screen
            Toast.makeText(requireContext(), "Sản phẩm của tôi", Toast.LENGTH_SHORT).show()
        }
        binding.textViewTransactionHistoryLink.setOnClickListener {
            Log.d(TAG, "Transaction History link clicked")
            // TODO: Navigate to transaction history screen
            Toast.makeText(requireContext(), "Lịch sử giao dịch", Toast.LENGTH_SHORT).show()
        }
        binding.textViewSavedItemsLink.setOnClickListener {
            Log.d(TAG, "Saved Items link clicked")
            // TODO: Navigate to saved items screen
            Toast.makeText(requireContext(), "Sản phẩm đã lưu", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupObservers() {
        profileViewModel.profileState.observe(viewLifecycleOwner) { state ->
            Log.d(TAG, "ProfileState observed: $state")
            binding.progressBarProfile.isVisible = state is ProfileState.Loading

            // Ẩn tất cả các view nội dung ban đầu, chỉ hiện khi có data
            binding.profileContentContainer.visibility = if (state is ProfileState.Success) View.VISIBLE else View.INVISIBLE


            when (state) {
                is ProfileState.Loading -> {
                    // ProgressBar đã xử lý
                }
                is ProfileState.Success -> {
                    bindUserData(state.user, state.isCurrentUserProfile)
                    // userItemListAdapter.submitList(state.items) // << SẼ BẬT LẠI KHI CÓ ADAPTER
                    // Log.d(TAG, "Submitted ${state.items.size} items to adapter.")

                    // Cập nhật lại menu để ẩn/hiện item đúng
                    requireActivity().invalidateOptionsMenu()
                }
                is ProfileState.Error -> {
                    Toast.makeText(requireContext(), "Lỗi: ${state.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error state: ${state.message}")
                    // Có thể hiển thị một view lỗi thay vì để trống
                }
                ProfileState.Idle -> {
                    // Trạng thái nghỉ
                }
            }
        }

        // Observer cho logout từ AuthViewModel
        authViewModel.logoutState.observe(viewLifecycleOwner) { isLoggedOut ->
            if (isLoggedOut) {
                Log.d(TAG, "Logout successful, navigating to login or start destination.")
                // TODO: Điều hướng về màn hình đăng nhập hoặc màn hình khởi đầu của app
                // Ví dụ, nếu Login là start destination của auth_nav và MainActivity là host của auth_nav
                // và bạn muốn pop hết backstack của auth_nav:
                // findNavController().popBackStack(R.id.auth_nav_graph_id, true) // auth_nav_graph_id là id của <navigation android:id="@+id/auth_nav_graph_id" ...>
                // findNavController().navigate(R.id.loginFragment) // Hoặc navigate tới màn hình cụ thể

                // Hoặc nếu bạn có một action global để đi về luồng auth từ bất cứ đâu:
                // findNavController().navigate(R.id.action_global_to_authFragment)
                Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                try {
                    // Ví dụ điều hướng về start destination của nav graph hiện tại (có thể là login)
                    val navGraph = findNavController().graph
                    findNavController().popBackStack(navGraph.startDestinationId, false)
                    // Hoặc nếu LoginFragment có ID cụ thể và là start destination của một graph khác thì navigate tới đó.
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating after logout", e)
                }

                authViewModel.onLogoutCompleted() // Reset cờ logout
            }
        }
    }

    private fun bindUserData(user: User, isCurrentUserProfile: Boolean) {
        binding.toolbarProfile.title = if (isCurrentUserProfile) "Hồ sơ của tôi" else user.displayName

        Glide.with(this)
            .load(user.profilePictureUrl)
            .placeholder(R.drawable.ic_person) // Ảnh mặc định
            .error(R.drawable.ic_person)       // Ảnh khi lỗi load
            .into(binding.imageViewProfilePicture)

        binding.textViewDisplayName.text = user.displayName
        binding.textViewEmail.text = user.email
        binding.textViewEmail.visibility = if (isCurrentUserProfile && !user.email.isNullOrEmpty()) View.VISIBLE else View.GONE

        binding.textViewBio.text = user.bio ?: "Chưa có tiểu sử."
        binding.textViewBio.visibility = if (!user.bio.isNullOrEmpty() || isCurrentUserProfile) View.VISIBLE else View.GONE


        val decimalFormat = DecimalFormat("#.#")
        binding.textViewAverageRating.text = if(user.averageRating > 0) "${decimalFormat.format(user.averageRating)} ★" else "Chưa có"
        binding.textViewTotalTransactions.text = user.totalTransactions.toString()
        binding.textViewTotalListings.text = user.totalListings.toString()

        // Thông tin liên hệ
        val contactInfo = user.contactInfo
        if (contactInfo?.phone != null && !contactInfo.phone.isNullOrEmpty()) {
            binding.textViewPhoneNumber.text = "SĐT: ${contactInfo.phone}"
            binding.textViewPhoneNumber.visibility = View.VISIBLE
            binding.labelContactInfo.visibility = View.VISIBLE
        } else {
            binding.textViewPhoneNumber.visibility = View.GONE
            // Ẩn luôn label nếu không có thông tin liên hệ nào
            if (contactInfo?.zalo.isNullOrEmpty() && contactInfo?.facebook.isNullOrEmpty()) {
                binding.labelContactInfo.visibility = View.GONE
            }
        }
        // TODO: Hiển thị Zalo, Facebook tương tự nếu có

        // Các action và danh sách sản phẩm
        if (isCurrentUserProfile) {
            binding.layoutMyProfileActions.visibility = View.VISIBLE
            binding.buttonSendMessage.visibility = View.GONE
            binding.buttonActionTopRight.text = "Chỉnh sửa"
            binding.buttonActionTopRight.setIconResource(R.drawable.ic_edit)
            binding.buttonActionTopRight.visibility = View.VISIBLE
            // Hiển thị danh sách sản phẩm của người dùng hiện tại nếu cần
            // binding.textViewUserListingsTitle.text = "Sản phẩm của bạn"
            // binding.textViewUserListingsTitle.visibility = View.VISIBLE
            // binding.recyclerViewUserListings.visibility = View.VISIBLE
        } else {
            binding.layoutMyProfileActions.visibility = View.GONE
            binding.buttonSendMessage.visibility = View.VISIBLE
            binding.buttonActionTopRight.visibility = View.GONE // Hoặc đổi thành nút "Theo dõi"/"Báo cáo"
            // Hiển thị danh sách sản phẩm của người dùng đang xem
            binding.textViewUserListingsTitle.text = "Sản phẩm của ${user.displayName}"
            // binding.textViewUserListingsTitle.visibility = View.VISIBLE
            // binding.recyclerViewUserListings.visibility = View.VISIBLE
        }
        // Tạm thời ẩn RecyclerView và title của nó
        binding.textViewUserListingsTitle.visibility = View.GONE
        binding.recyclerViewUserListings.visibility = View.GONE

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView called")
    }
}