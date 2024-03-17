import { useEffect, useState } from "react";
import axios from "axios";
import Post from "./Post";
import "../styles/feedPosts.css";

const PostsList = ({ queryString, category, newPost, toggleEditPost, setPostToEdit, refreshedPost }) => {
  const [posts, setPosts] = useState();

  useEffect(() => {
    let query = queryString + (category !== undefined ? `&categoryId=${category}` : "");
    axios.get(query).then(result => {
      setPosts(JSON.parse(result.data.data));
    });
  }, [category]);

  useEffect(() => {
    if (newPost && (posts !== undefined)) {
      setPosts(posts => [newPost, ...posts]);
    }
  }, [newPost]);

  if (posts === undefined) {
    return <></>
  }

  if (posts.length === 0) {
    return <h2 style={{padding: "5.55vw 0", fontSize: "1.5em", textAlign: "center"}}>В этой ленте ещё нет постов</h2>
  }
  
  return (
    <div className="postsContainer">
      {posts.map(post => {
        return <Post key={post.id} data={post}
          toggleEditPost={toggleEditPost}
          setPostToEdit={setPostToEdit}
          refreshedPost={refreshedPost}
        />
      })}
    </div>
  );
}

export default PostsList
