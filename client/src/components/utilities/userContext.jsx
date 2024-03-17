import React, { useContext, useState, useEffect } from 'react';
import axios from 'axios';

export const UserContext = React.createContext();

export const useUser = () => {
  return useContext(UserContext);
}

export const UserProvider = ({ ident, children }) => {
  const [id, setId] = useState(ident);
  const [avatar, setAvatar] = useState();
  const [errorOpen, setErrorOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState();
  const [selectedPost, setSelectedPost] = useState();
  const [identificator, setIdentificator] = useState();

  const signIn = (newUser, cb) => {
    setId(newUser);
    localStorage.setItem('userId', newUser);
    cb();
  }

  const signOut = (cb) => {
    setId(null);
    localStorage.removeItem('userId');
    cb();
  }

  useEffect(() => {
    if (id) {
      axios.get(`http://localhost:8080/getProfile?userId=${id}`).then(result => {
        const data = JSON.parse(result.data.data)
        setIdentificator(data.identificator);
        setAvatar(data.imagePath);
      });
    }
  }, [id]);

  if ((!identificator) && (id)) {
    return <></>
  }

  const toggleError = () => {
    setErrorOpen(prev => !prev);
  }

  return (
    <UserContext.Provider value={{
      id: id,
      avatar: avatar,
      identificator: identificator,
      selectedPost: selectedPost,
      errorOpen: errorOpen,
      errorMessage: errorMessage,
      toggleError,
      setSelectedPost, setErrorMessage, setAvatar,
      signIn, signOut
    }}>
      { children }
    </UserContext.Provider>
  );
}
