package com.tarripoha.android.paging;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tarripoha.android.R;
import com.tarripoha.android.data.db.Comment;

@SuppressWarnings("ConstantConditions")
public class CommentListLiveData extends LiveData<Operation> implements EventListener<QuerySnapshot> {
    private Query query;
    private ListenerRegistration listenerRegistration;
    private OnLastVisibleCommentCallback onLastVisibleCommentCallback;
    private OnLastCommentReachedCallback onLastCommentReachedCallback;

    CommentListLiveData(Query query, OnLastVisibleCommentCallback onLastVisibleCommentCallback, OnLastCommentReachedCallback onLastCommentReachedCallback) {
        this.query = query;
        this.onLastVisibleCommentCallback = onLastVisibleCommentCallback;
        this.onLastCommentReachedCallback = onLastCommentReachedCallback;
    }

    @Override
    protected void onActive() {
        listenerRegistration = query.addSnapshotListener(this);
    }

    @Override
    protected void onInactive() {
        listenerRegistration.remove();
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
        if (e != null) return;

        for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
            switch (documentChange.getType()) {
                case ADDED:
                    Comment addedComment = documentChange.getDocument().toObject(Comment.class);
                    Operation addOperation = new Operation(addedComment, R.string.added);
                    setValue(addOperation);
                    break;

                case MODIFIED:
                    Comment modifiedComment = documentChange.getDocument().toObject(Comment.class);
                    Operation modifyOperation = new Operation(modifiedComment, R.string.modified);
                    setValue(modifyOperation);
                    break;

                case REMOVED:
                    Comment removedComment = documentChange.getDocument().toObject(Comment.class);
                    Operation removeOperation = new Operation(removedComment, R.string.removed);
                    setValue(removeOperation);
            }
        }

        int querySnapshotSize = querySnapshot.size();
        if (querySnapshotSize < 15) {
            onLastCommentReachedCallback.setLastCommentReached(true);
        } else {
            DocumentSnapshot lastVisibleComment = querySnapshot.getDocuments().get(querySnapshotSize - 1);
            onLastVisibleCommentCallback.setLastVisibleComment(lastVisibleComment);
        }
    }

    interface OnLastVisibleCommentCallback {
        void setLastVisibleComment(DocumentSnapshot lastVisibleComment);
    }

    interface OnLastCommentReachedCallback {
        void setLastCommentReached(boolean isLastCommentReached);
    }
}