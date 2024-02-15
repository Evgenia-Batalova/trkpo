import { useState } from "react";
import PostsList from "../components/PostsList";
import { useUser } from "../components/utilities/userContext";
import "../styles/news.css"

const News = () => {
  const user = useUser();
  const [category, setCategory] = useState();

  return (
    <div className="pageContainer">
      <ul className="categoriesList">
        <li className="categoryItem" onClick={() => setCategory(1)}>
          <img src="postfeed/naruto.png" alt="It" />
        </li>
        <li className="categoryItem" onClick={() => setCategory(3)}>
          <img src="postfeed/piece.png" alt="Games" style={{border: "2px solid black"}} />
        </li>
        <li className="categoryItem" onClick={() => setCategory(2)}>
          <img src="postfeed/bleach.png" alt="Kino" />
        </li>
      </ul>
      <div className="headersContainer">
        <div className="headerLeft">
          <img src="postfeed/newsheader.PNG" alt="NewsHeader" />
        </div>
      </div>
      <div className="feed">
        <PostsList queryString={`http://localhost:8080/getPosts?userId=${user.id}`}
          category={category}
        />
      </div>
    </div>
  );
}

export default News
