import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import axios from "axios";
import UserProfile from "../../pages/UserProfile";
import * as user from "../../components/utilities/userContext";

const profileData = {
  name: "naso337",
  identificator: "naso123",
  imagePath: "image.jpg",
  follows: 3,
  followers: 5,
  info: "Some profile info"
}

function mockCallGet(mockData) {
  axios.get.mockResolvedValue({
    data: {
      state: "Success",
      message: "",
      data: mockData
    }
  });
}

function mockCallPut() {
  axios.put.mockResolvedValue({
    data: {
      state: "Success",
      message: "",
      data: {}
    }
  });
}

jest.mock('../../components/PostsList.jsx', () => () => <></>);
jest.mock('../../components/modals/createPostModal.jsx', () => ({ isOpen }) => <></>);

test('Profile data change test', async () => {
  jest.spyOn(user, 'useUser').mockImplementation(() => {
    return {
      id: 1
    }
  });

  mockCallGet(JSON.stringify(profileData));

  await waitFor(() => {
    render(<UserProfile />);
  });

  await waitFor(() => {
    expect(screen.getByAltText("Change")).toBeVisible();
  });

  userEvent.click(screen.getByAltText("Change"));

  const inputString = "text";

  userEvent.type(screen.getByPlaceholderText("Введите имя"), inputString);
  userEvent.type(screen.getByPlaceholderText("Введите идентификатор"), inputString);
  userEvent.type(screen.getByPlaceholderText("Расскажите о себе"), inputString);

  mockCallPut();

  await waitFor(() => {
    userEvent.click(screen.getByRole('button', { name: 'Сохранить' }));
  });

  await waitFor(() => {
    expect(screen.getByText(profileData.info + inputString));
    expect(screen.getByText("@" + profileData.identificator + inputString));
    expect(screen.getByText(profileData.name + inputString));
    expect(screen.queryByRole('button', { name: 'Сохранить' })).toBeNull();
  });

});
