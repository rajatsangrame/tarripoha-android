package com.tarripoha.android.paging;

import androidx.lifecycle.ViewModel;

@SuppressWarnings("WeakerAccess")
public class CommentListViewModel extends ViewModel {
    //private CommentListRepository commentListRepository = new FirestoreCommentListRepositoryCallback();

    CommentListLiveData getCommentListLiveData() {
        return null;
        //return commentListRepository.getCommentListLiveData();
    }

    interface CommentListRepository {
        CommentListLiveData getCommentListLiveData();
    }
}