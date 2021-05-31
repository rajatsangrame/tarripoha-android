package com.tarripoha.android.paging;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.google.firebase.firestore.Query.Direction.ASCENDING;
import static com.tarripoha.android.paging.Constants.*;

public class FirestoreCommentListRepositoryCallback implements CommentListViewModel.CommentListRepository,
        CommentListLiveData.OnLastVisibleCommentCallback, CommentListLiveData.OnLastCommentReachedCallback {
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference productsRef = firebaseFirestore.collection(COMMENT_COLLECTION);
    private Query query = productsRef.orderBy(COMMENT_NAME_PROPERTY, ASCENDING).limit(LIMIT);
    private DocumentSnapshot lastVisibleProduct;
    private boolean isLastProductReached;

    @Override
    public CommentListLiveData getCommentListLiveData() {
        if (isLastProductReached) {
            return null;
        }
        if (lastVisibleProduct != null) {
            query = query.startAfter(lastVisibleProduct);
        }
        return new CommentListLiveData(query, this, this);
    }

    @Override
    public void setLastVisibleComment(DocumentSnapshot lastVisibleProduct) {
        this.lastVisibleProduct = lastVisibleProduct;
    }

    @Override
    public void setLastCommentReached(boolean isLastProductReached) {
        this.isLastProductReached = isLastProductReached;
    }
}