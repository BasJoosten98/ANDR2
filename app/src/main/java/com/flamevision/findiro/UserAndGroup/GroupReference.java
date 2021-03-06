package com.flamevision.findiro.UserAndGroup;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupReference extends Group {

    public interface GroupReferenceUpdate{
        void GroupValuesUpdated(@NonNull Group oldGroup, @NonNull GroupReference newGroup);
    }
    private List<GroupReference.GroupReferenceUpdate> listeners = new ArrayList<>();

    private boolean updatedOnce;
    private boolean updateErrorOccurred;

    private DatabaseReference groupRef;

    private static final  String log = "GroupReference";
    private static boolean printUpdate = true;

    public GroupReference(@NonNull String groupId, GroupReferenceUpdate listener){
        super();
        this.groupId = groupId;
        this.updatedOnce = false;
        this.updateErrorOccurred = false;
        if(listener != null){AddListener(listener);}
        setupReference();
    }

    public void AddListener(GroupReferenceUpdate listener){
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    public void RemoveListener(GroupReferenceUpdate listener){
        listeners.remove(listener);
    }
    private void setupReference(){
        groupRef = FirebaseDatabase.getInstance().getReference("Groups/" + groupId);
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updatedOnce = true;
                updateValues(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                updateErrorOccurred = true;
                Log.e(log, "Database error occurred: " + databaseError.getMessage());
            }
        });
    }
    public boolean DeleteGroup(){
        if(groupCreator != null){
            FirebaseUser curLoggedInUser = FirebaseAuth.getInstance().getCurrentUser();
            if(curLoggedInUser != null){
                if(curLoggedInUser.getUid().equals(groupCreator)){
                    for(String userId: members){
                        final String curGroupId = groupId;
                        final String curUserId = userId;
                        DatabaseReference userGroupRef = FirebaseDatabase.getInstance().getReference("Users/" + curUserId + "/groups");
                        userGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot child: dataSnapshot.getChildren()){
                                    if(child.getValue() != null){
                                        String userGroupId = child.getValue().toString();
                                        if(userGroupId.equals(curGroupId)){
                                            DatabaseReference curUserGroupRef = FirebaseDatabase.getInstance().getReference("Users/" + curUserId + "/groups/" + child.getKey());
                                            curUserGroupRef.removeValue();
                                            break;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    groupRef.removeValue();
                    return true;
                }
            }
        }
        return false;
    }

    private void updateValues(DataSnapshot dataSnapshot){
        Group oldGroup = GetCurrentGroup();

        Object oName = dataSnapshot.child("name").getValue();
        if(oName != null){
            name = oName.toString();
        }
        else {name = null;}

        Object oGroupCreator = dataSnapshot.child("groupCreator").getValue();
        if(oGroupCreator != null){
            groupCreator = oGroupCreator.toString();
        }
        else {groupCreator = null;}

        members = new ArrayList<>();
        for(DataSnapshot groupIdSnapShot : dataSnapshot.child("members").getChildren()){
            Object oMemberId = groupIdSnapShot.getValue();
            if(oMemberId != null){
                members.add(oMemberId.toString());
            }
        }

        if(printUpdate){printGroup();}
        updateAllListeners(oldGroup);
    }
    public Group GetCurrentGroup(){
        List<String> tempMembers = new ArrayList<>();
        for(String s: members){tempMembers.add(s);}
        Group temp = new Group(groupId, name, groupCreator, tempMembers);
        return temp;
    }
    private void updateAllListeners(@NonNull Group oldGroup){
        for(GroupReferenceUpdate listener : listeners){
            if(listener != null) {
                listener.GroupValuesUpdated(oldGroup, this);
            }
        }
    }

    private void printGroup(){
        Log.e(log, toString());
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isUpdatedOnce() {
        return updatedOnce;
    }

    public boolean isUpdateErrorOccurred() {
        return updateErrorOccurred;
    }
    public DatabaseReference getGroupRef(){return groupRef;}
}
