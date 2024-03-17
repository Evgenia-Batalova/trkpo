import { Route, Routes, Navigate } from 'react-router-dom';
import News from '../pages/News';
import Subscriptions from '../pages/Subscriptions';
import Profile from '../pages/Profile';
import UserProfile from '../pages/UserProfile';
import Layout from './Layout';
import { useUser } from './utilities/userContext';
import LoginPage from '../pages/LoginPage';
import RequireAuth from './utilities/RequireAuth';
import NotFound from '../pages/NotFound';

const App = () => {
  const user = useUser();
  
  return (
    <>
      <Routes>
        <Route path="login/*" element={<LoginPage />}></Route>
        <Route path="login" element={<Navigate to="auth" replace />} />
        <Route path="/" element={<Layout />}>
          <Route index element={
            <RequireAuth>
              <News />
            </RequireAuth>
          }></Route>
          <Route path="subs" element={
            <RequireAuth>
              <Subscriptions />
            </RequireAuth>
          }></Route>
          <Route path="profile/:identifier" element={
            <RequireAuth>
              <Profile />
            </RequireAuth>}></Route>
          <Route path={`profile/${user.identificator}`}  element={
            <RequireAuth>
              <UserProfile />
            </RequireAuth>
          }></Route>
        </Route>
        <Route path="*" element={<NotFound />}></Route>
      </Routes>
    </>
  );
}

export default App
