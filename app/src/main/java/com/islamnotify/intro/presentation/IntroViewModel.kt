package com.islamnotify.intro.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.islamnotify.main.domain.PermissionDialogs

class IntroViewModel : ViewModel() {

    val visiblePermissionDialogQueue = mutableStateListOf<PermissionDialogs>()

    fun dismissDialog(){
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: PermissionDialogs?,
        isGranted: Boolean
    ){
        if (!isGranted && permission != null && !visiblePermissionDialogQueue.contains(permission)){
            visiblePermissionDialogQueue.add(permission)
        }
    }


}