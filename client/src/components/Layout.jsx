import React, { Suspense } from 'react';
import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useUser } from './utilities/userContext';
import "../styles/layout.css";

const ErrorModal = React.lazy(() => import('./modals/errorModal'));

const Layout = () => {
  const user = useUser();
  const navigate = useNavigate();

  return (
    <>
      <Suspense>
        <ErrorModal />
      </Suspense>

      <div className="main">
        <div className='menu'>
          <Link className="avatar" to={`profile/${user.identificator}`}>
            <img src={user.avatar} alt="Avatar" style={{display: user.avatar === undefined ? "none" : "block"}} />
          </Link>
          <Link to="/"><img className='menuButton' src="layout/news.PNG" alt="News" style={{margin: '2rem 0 0.7rem'}} /></Link> 
          <Link to="/subs"><img className='menuButton' src="layout/subs.PNG" alt="Subs" /></Link> 
          <img className='menuButton' src="layout/exit.PNG" alt="Exit" 
            onClick={() => {
              user.signOut(() => navigate('/', {replace: true}))
            }}
          />
        </div>
        <Outlet />
      </div>
    </>
  );
}

export default Layout
