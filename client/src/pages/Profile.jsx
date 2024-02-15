import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import { useUser } from "../components/utilities/userContext";
import NotFound from "./NotFound";
import PostsList from "../components/PostsList";
import "../styles/profile.css";

const Profile = () => {
  const user = useUser();
  const { identifier } = useParams();
  const [profileId, setProfileId] = useState();
  const [profileData, setProfileData] = useState();
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [isNotFound, setIsNotFound] = useState(false);

  useEffect(() => {
    axios.get(`http://localhost:8080/getIdByIdentificator?identificator=${identifier}`).then(result => {
      if (result.data.data === -1) {
        setIsNotFound(true);
      }
      else {
        setProfileId(result.data.data);
        axios.get(`http://localhost:8080/getProfile?userId=${result.data.data}`).then(res => {
          setProfileData(JSON.parse(res.data.data));
          axios.get(`http://localhost:8080/getIsSubscribe?followerId=${user.id}&userId=${result.data.data}`).then(ress => {
            if (ress.data.data) {
              setIsSubscribed(true);
            }
          });
        });
      }
    });
  }, [identifier, user.id]);
  
  if(isNotFound) {
    return <NotFound></NotFound>
  }

  if (profileData === undefined) {
    return <></>
  }

  return (
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
          <img className="profileInfoButton"
            src={isSubscribed ? "profile/unsubscribe.png" : "profile/subscribe.png"}
            alt="Subscribe"
            onClick={() => {
              if (isSubscribed) {
                axios.delete("http://localhost:8080/unfollowUser", {
                  data: {
                    followerId: user.id,
                    userId: profileId
                  }
                }).then(result => {
                  if (result.data.state === "Success") {
                    setProfileData(prev => {
                      return {
                        ...prev,
                        followers: prev.followers - 1
                      }
                    });
                    setIsSubscribed(false);
                  } else {
                    user.setErrorMessage(result.data.message);
                    user.toggleError();
                  }
                });
              } else {
                axios.post("http://localhost:8080/followUser", {
                  followerId: user.id,
                  userId: profileId
                }).then(result => {
                  if (result.data.state === "Success") {
                    setProfileData(prev => {
                      return {
                        ...prev,
                        followers: prev.followers + 1
                      }
                    });
                    setIsSubscribed(true);
                  } else {
                    user.setErrorMessage(result.data.message);
                    user.toggleError();
                  }
                });
              }
            }}
          />
        </div>
        <div className="infoColumn">
          <div className="infoText">
            <p>Подписки: {profileData.follows}</p>
            <p className="followersCount">Подписчики: {profileData.followers}</p>
          </div>
        </div>
      </div>
      <p className="about" style={{textAlign: profileData.info === "" ? "center" : ""}}>
        {profileData.info === "" ? "Информация не указана" : profileData.info}
      </p>
      <div style={{background: "#cfa452"}}> 
        <PostsList queryString={`http://localhost:8080/getUserPosts?userId=${profileId}`} />
      </div>
    </div>
  );
}

export default Profile
