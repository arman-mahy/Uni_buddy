package com.example.homepage.features.groupTab.Group.repository

import androidx.lifecycle.MutableLiveData
import com.example.homepage.utils.models.GroupData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class UserGroupRepo {

    val auth = Firebase.auth
    val user = auth.currentUser!!.uid
    private val groupReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("user-groups").child(user)
    private var INSTANCE: UserGroupRepo? = null

    fun getInstance(): UserGroupRepo {
        return INSTANCE ?: synchronized(this) {
            val instance = UserGroupRepo()
            INSTANCE = instance
            instance
        }
    }

    fun loadUserGroups(allGroups: MutableLiveData<List<GroupData>>) {

        groupReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val groupList: List<GroupData> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(GroupData::class.java)!!
                    }
                    allGroups.postValue(groupList)
                } catch (_: Exception) {

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        groupReference.keepSynced(true)
    }
}