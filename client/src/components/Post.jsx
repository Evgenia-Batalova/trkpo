import { useState, useEffect } from "react";
import { Link } from 'react-router-dom';
import axios from "axios";
import { useUser } from "./utilities/userContext";
import { getDateFormat } from "../js/functions";
import "../styles/feedPosts.css";

const Post = ({ data }) => {
  const user = useUser();
  const [authorAvatar, setAuthorAvatar] = useState(data.authorImage);
  const [category, setCategory] = useState(data.category);
  const [text, setText] = useState(data.text);
  const [image, setImage] = useState(data.image);
  const [isLiked, setIsLiked] = useState(data.isLiked);
  const [likes, setLikes] = useState(data.likesCount);
  const [deleted, setDeleted] = useState(false);

  useEffect(() => {
    if ((data.authorId === user.id) && (data.authorImage !== user.avatar)) {
      setAuthorAvatar(user.avatar);
    }
  }, [user.avatar]);

  if (deleted) {
    return <></>
  }

  const increaseLikes = async () => {
    await axios.put(`http://localhost:8080/incLikesOnPost?userId=${user.id}&postId=${data.id}`).then(result => {
      if (result.data.state === "Success") {
        setIsLiked(true);
        setLikes(prev => prev + 1);
      } else {
        user.setErrorMessage(result.data.message);
        user.toggleError();
      }
    });
  }

  const decreaseLikes = async () => {
    await axios.put(`http://localhost:8080/decLikesOnPost?userId=${user.id}&postId=${data.id}`).then(result => {
      if (result.data.state === "Success") {
        setIsLiked(false);
        setLikes(prev => prev - 1);
      } else {
        user.setErrorMessage(result.data.message);
        user.toggleError();
      }
    });
  }

  return (
    <>
      <div className="postContent">
        <div className="postInfo">
          <div className="postUserInfo" style={{width: data.authorId !== user.id ? "100%" : "67%"}}>
            <Link to={`/profile/${data.identificator}`}>
              <img src={authorAvatar} alt="AvatarCirclePost" />
            </Link>
            <div style={{marginTop: "0.15vw", textAlign: "center"}}>
              <p style={{fontSize: '1.2em'}}>{data.name}</p>
              <p>@{data.identificator}</p>
            </div>
          </div>
          {data.authorId !== user.id ? <></> :
            <div className="postButtons">
              <button onClick={() => {
                  axios.delete(`http://localhost:8080/deletePost?postId=${data.id}`).then(result => {
                    if (result.data.state === "Success") {
                      setDeleted(true);
                    } else {
                      user.setErrorMessage(result.data.message);
                      user.toggleError();
                    }
                  });
                }}
              >
                <img src="common/delete.jpg" alt="Delete" />
              </button>
            </div>  
          }
          <div className="postDate">
            <p>{getDateFormat(data.time)}</p>
            <p className="postCategory">{category}</p>
          </div>
        </div>
        <div className="postMainContent">
          <p dangerouslySetInnerHTML={{__html: text}}></p>
          <img src={image} alt="Post Img"
            style={{display: image === "" ? "none" : "block"}}
          />
          
        </div>
        <div className="likeContainer">
            <img src={isLiked ? "post/redlike.png" : "post/whitelike.png"} alt="Like"
              onClick={async () => {
                if (!isLiked) {
                  await increaseLikes();
                } else {
                  await decreaseLikes();
                }
              }}
            />
            <p className="likesCount">{likes}</p>
        </div>
      </div>
    </>
  );
}

export default Post
