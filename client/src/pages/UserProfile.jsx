import React, { Suspense, useEffect, useState } from "react";
import axios from "axios";
import PostsList from "../components/PostsList";
import { useUser } from "../components/utilities/userContext";
import "../styles/profile.css";

const CreatePostModal = React.lazy(() => import("../components/modals/createPostModal"));
const EditUserModal = React.lazy(() => import("../components/modals/editUserModal"));

const UserProfile = () => {
  const user = useUser();
  const [profileData, setProfileData] = useState();
  const [newPost, setNewPost] = useState();
  
  const [createPostOpen, setCreatePostOpen] = useState(false);
  const [editUserOpen, setEditUserOpen] = useState(false);

  useEffect(() => {
    axios.get(`http://localhost:8080/getProfile?userId=${user.id}`).then(result => {
      setProfileData(JSON.parse(result.data.data));
    });
  }, [user.id]);
  
  if (profileData === undefined) {
    return <></>
  }

  const toggleCreatePost = () => {
    setCreatePostOpen(prev => !prev);
  }

  const toggleInfoEditing = () => {
    setEditUserOpen(prev => !prev);
  }

  return (
    <>
      <Suspense>
        <CreatePostModal isOpen={createPostOpen}
          toggle={toggleCreatePost}
          setNewPost={setNewPost} 
          userName={profileData.name}
        />
        <EditUserModal isOpen={editUserOpen}
          toggle={toggleInfoEditing}
          data={profileData}
          setProfileData={setProfileData}
        />
      </Suspense>

      <div className="pageContainer profile">
        <div className="profileInfo">
          <div className="infoColumn">
            <div className="infoText">
              <p>{profileData.name}</p>
              <p>@{profileData.identificator}</p>
            </div>
          </div>
          <div className="infoColumn">
            <img className="userAvatar" src={`${profileData.imagePath}`} alt="Avatar2" />
            <img className="profileInfoButton" src="profile/change.png" alt="Change"
              onClick={() => {
                toggleInfoEditing();
              }}
            />
          </div>
          <div className="infoColumn">
            <div className="infoText">
              <p>Подписки: {profileData.follows}</p>
              <p>Подписчики: {profileData.followers}</p>
            </div>
          </div>
        </div>
        <p className="about" style={{textAlign: profileData.info === "" ? "center" : ""}}>
          {profileData.info === "" ? "Информация не указана" : profileData.info}
        </p>
        <div style={{textAlign: "center"}}>
          <img className="createPostButton" src="profile/createpost.png" alt="CreatePost" 
            onClick={() => {
              toggleCreatePost();
            }}
          />
        </div>
        <div style={{background: "#cfa452"}}>
          <PostsList queryString={`http://localhost:8080/getUserPosts?userId=${user.id}`}
            newPost={newPost}
            setNewPost={setNewPost}
          />
        </div>
      </div>
    </>
  );
}

export default UserProfile
