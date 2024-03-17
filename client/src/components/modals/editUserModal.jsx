import { useState, useEffect, useCallback } from "react";
import axios from "axios";
import { useUser } from "../utilities/userContext";
import { debounce } from "lodash";
import "../../styles/modals/modal.css";
import "../../styles/modals/editUser.css";

const EditUserModal = ({ isOpen, toggle, data, setProfileData }) => {
  const user = useUser();
  const [name, setName] = useState(data.name);
  const [identifier, setIdentifier] = useState(data.identificator);
  const [infoText, setInfoText] = useState(data.info);
  const [avatar, setAvatar] = useState(data.imagePath);
  const [isDisabled, setIsDisabled] = useState(true);
  const [selectedImage, setSelectedImage] = useState(null);
  const [message, setMessage] = useState();

  useEffect(() => {
    setName(data.name);
    setIdentifier(data.identificator);
    setInfoText(data.info);
    setAvatar(data.imagePath);
  }, [isOpen]);

  const handler = useCallback(debounce(checkID, 1500), []);

  if(!isOpen) {
    return null;
  }

  const handleName = (e) => {
    const value = e.target.value.replace(/[^a-zа-я0-9]/gi, '');
    setName(value);
    setDisabled(value, identifier, infoText);
  };

  const handleIdentifier = (e) => {
    const value = e.target.value.replace(/[^a-z0-9]/gi, '');
    
    handler(value);
    setIdentifier(value);
    setDisabled(name, value, infoText);
  };

  const handleInfo = (e) => {
    setInfoText(e.target.value);
    setDisabled(name, identifier, e.target.value);
  };

  const setDisabled = (name, identifier, info) => {
    if ((data.name === name) && (data.identificator === identifier)
        && (data.info === info) && (selectedImage === null)) {
      setIsDisabled(true);
    } else {
      setIsDisabled(false);
    }
  }

  function checkID(idef) {
    axios.get(`http://localhost:8080/verifyIdentificator?identificator=${idef}&userId=${user.id}`).then(result => {
      if (result.data.state === "Error") {
        user.setErrorMessage(result.data.message);
        user.toggleError();
      } else {
        if (result.data.data) {
          setMessage("Идентификатор свободен");
        } else {
          setMessage("Идентификатор занят");
        }
      }
    });
  }

  return (
    <div className='overlay'>
      <div className='modalContainer' style={{padding: "2vw", height: "auto"}}>
        <div className="editUser">
          <div className="editUserAvatar">
            <img src={selectedImage !== null ? URL.createObjectURL(selectedImage) : avatar} alt="Avatar" />
            <label className="newAvatarBtn" htmlFor="#loadUserAvatar">Загрузить</label>
            <input id="#loadUserAvatar" type="file"
              accept="image/png, image/jpg, image/jpeg"
              onChange={(event) => {
                  let reader = new FileReader();
                  reader.readAsDataURL(event.target.files[0]);
                  reader.onload = function (e) {
                    let image = new Image();
                    image.src = e.target.result;

                    image.onload = function () {
                      if (event.target.files[0].size > (1024 * 1024 * 5)) {
                        user.setErrorMessage("Размер файла слишком большой");
                        user.toggleError();
                      } else if ((this.width !== this.height) || (this.width > 4096)) {
                        user.setErrorMessage("Изображение должно быть квадратным и быть меньше 4096x4096");
                        user.toggleError();
                      } else {
                        setSelectedImage(event.target.files[0]);
                        setIsDisabled(false);
                      }
                    };
                  };
              }}
              style={{display: "none"}}
            />
          </div>
          <div className="editUserInformation">
            <img src="profile/change.png" alt="EditProfile" />
            <input type="text" placeholder="Введите имя"
              value={name}
              onChange={handleName}
              maxLength={30}
            />
            <input type="text" placeholder="Введите идентификатор"
              value={identifier}
              onChange={handleIdentifier}
              maxLength={30}
            />
            <p style={{display: message ? "block" : "none"}}>{message}</p>
          </div>
        </div>
        <textarea name="" id="" className="editDescription"
          placeholder="Расскажите о себе"
          maxLength="1000"
          value={infoText}
          onChange={handleInfo}
        ></textarea>
        <div className="confirmUserChanges">
          <button
            onClick={() => {
              setSelectedImage(null);
              setIsDisabled(true);
              toggle();
            }}
          >
            Отменить
          </button>
          <button
            disabled={isDisabled}
            onClick={async () => {
              let image;
              if (selectedImage !== null) {
                const reader = new FileReader();
                
                reader.readAsDataURL(selectedImage);
                reader.onloadend = async () => {
                  image = reader.result.replace('data:', '').replace(/^.+,/, '');
                  await changeProfile(user.id, identifier, name, infoText, image).then(result => {
                    if (result.data.state === "Error") {
                      user.setErrorMessage(result.data.message);
                      user.toggleError();
                    } else {
                      setProfileData({
                        ...data,
                        name: name,
                        identificator: identifier,
                        info: infoText,
                        imagePath: result.data.data
                      });
                      user.setAvatar(result.data.data);
                      setSelectedImage(null);
                      setIsDisabled(true);
                      toggle();
                    }
                  });
                };
              } else {
                image = data.imagePath;
                await changeProfile(user.id, identifier, name, infoText, image).then(result => {
                  if (result.data.state === "Error") {
                    user.setErrorMessage(result.data.message);
                    user.toggleError();
                  } else {
                    setProfileData({
                      ...data,
                      name: name,
                      identificator: identifier,
                      info: infoText,
                      imagePath: image
                    });
                    setSelectedImage(null);
                    setIsDisabled(true);
                    toggle();
                  }
                });
              }
            }}
          >
            Сохранить
          </button>
        </div>
      </div>
    </div>
  );
};

async function changeProfile(id, identifier, name, info, img) {
  return await axios.put("http://localhost:8080/changeProfile", {
    id: id,
    identificator: identifier.toLowerCase(),
    name: name,
    info: info,
    imagePath: `${img}`
  }).then(result => {
    return result;
  });
}

export default EditUserModal
