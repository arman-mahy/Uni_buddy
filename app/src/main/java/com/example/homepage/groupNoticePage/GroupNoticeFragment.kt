package com.example.homepage.groupNoticePage

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homepage.R
import com.example.homepage.databinding.FragmentGroupNoticeBinding
import com.example.homepage.groupNoticePage.groupNoticeAdapter.GroupNoticeAdapter
import com.example.homepage.groupNoticePage.groupNoticeModel.GroupNoticeData
import com.example.homepage.groupNoticePage.groupNoticeModel.GroupNoticeViewModel
import com.example.homepage.superClass.ReplaceFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class GroupNoticeFragment(private var groupId: String = "") : ReplaceFragment() {
    private var _binding: FragmentGroupNoticeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: GroupNoticeViewModel
    private lateinit var recycler: RecyclerView
    private var adapter: GroupNoticeAdapter? = null
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var _inflater: LayoutInflater

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container?.removeAllViews()
        _binding = FragmentGroupNoticeBinding.inflate(inflater, container, false)
        _inflater = inflater
        auth = Firebase.auth

        val user = auth.currentUser!!.uid
        binding.floatingActionButton.setOnClickListener {

            binding.informativeText.text = ""
            val rootLayout = layoutInflater.inflate(R.layout.popup_add_notice, null)

            val taskName = rootLayout.findViewById<EditText>(R.id.QuizNamePop)
            val taskDescription = rootLayout.findViewById<EditText>(R.id.TaskDescriptionPop)
            val taskDate = rootLayout.findViewById<EditText>(R.id.TaskDatePop)
            val closeButton = rootLayout.findViewById<Button>(R.id.CloseButton)
            val addButton = rootLayout.findViewById<Button>(R.id.AddButton)

            val popupWindow = PopupWindow(
                rootLayout,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true
            )

            popupWindow.update()
            popupWindow.elevation = 20.5F
            popupWindow.showAtLocation(

                binding.ToDoActivity, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                -500// Y offset
            )

            closeButton.setOnClickListener {
                popupWindow.dismiss()
            }

            addButton.setOnClickListener {

                val name = taskName.text.toString()
                val description = taskDescription.text.toString()
                val date = taskDate.text.toString()

                if (name == "" || description == "")
                    Toast.makeText(
                        context,
                        "Please fill up all  the information",
                        Toast.LENGTH_SHORT
                    ).show()
                else
                    writeNewTask(user, name, description, date, "make-key", groupId)



                popupWindow.dismiss()
            }

        }
        return binding.root
    }

    fun writeNewTask(
        userId: String,
        taskName: String,
        taskDescription: String,
        taskDate: String,
        oldKey: String,
        groupId: String
    ) {
        database = Firebase.database.reference
        val newKey = database.child("posts").push().key
        if (newKey == null) {
            Log.w("TodoActivity", "Couldn't get push key for posts")
            return
        }
        val newGroupNotice: Any
        val childUpdates: Any

        if (oldKey == "make-key") {
            newGroupNotice = GroupNoticeData(userId, taskName, taskDescription, taskDate, newKey, groupId)
            val noticeValues = newGroupNotice.toMap()
            childUpdates = hashMapOf<String, Any>(
                "/group-notice/${this.groupId}/$newKey" to noticeValues
            )
        } else {
            newGroupNotice =
                GroupNoticeData(userId, taskName, taskDescription, taskDate, oldKey, groupId)
            val noticeValues = newGroupNotice.toMap()
            childUpdates = hashMapOf<String, Any>(
                "/group-notice/$groupId/$oldKey" to noticeValues
            )
        }

        database.updateChildren(childUpdates)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = binding.taskList
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.setHasFixedSize(true)
        adapter = GroupNoticeAdapter(_inflater)
        recycler.adapter = adapter
        viewModel = ViewModelProvider(this)[GroupNoticeViewModel::class.java]

        viewModel.initialize(groupId)

        viewModel.allSchedules.observe(viewLifecycleOwner) {
            adapter!!.updateUserList(it)
        }

    }

}