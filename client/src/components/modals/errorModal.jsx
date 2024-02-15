import { useUser } from "../utilities/userContext";
import "../../styles/modals/modal.css";

const ErrorModal = () => {
  const user = useUser();

  if(!user.errorOpen) {
    return null;
  }

  return (
    <div className='overlay'
      onClick={user.toggleError}
      style={{zIndex: "100"}}
    >
      <div className='modalContainer notificationsModal errorModal'
        onClick={(e) => {
          e.stopPropagation();
        }}
      >
        <div className="errorContent">
          <p className="errorTitle">Ошибка</p>
          <p className="errorMessage">{user.errorMessage}</p>
          <button className="okBtn" onClick={user.toggleError}>OK</button>
        </div>
      </div>
    </div>
  );
};

export default ErrorModal
