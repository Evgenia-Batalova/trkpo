import { useLocation, Navigate } from "react-router-dom";
import { useUser } from "./userContext";

const RequireAuth = ({ children }) => {
  const location = useLocation();
  const user = useUser();

  if (!user.id) {
    return <Navigate to="/login" state={{from: location}} />
  }

  return children;
}

export default RequireAuth
