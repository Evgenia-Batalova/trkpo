import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import axios from "axios";
import UserProfile from "../../pages/UserProfile";
import * as user from "../../components/utilities/userContext";

const profileData = {
  name: "naso337",
  identificator: "user1",
  imagePath: "image.jpg",
  follows: 3,
  followers: 5,
  info: "Some profile info"
}

const postData = {
  authorId: 1,
  authorImage: "image.jpg",
  category: "Наука",
  id: 1,
  identificator: "user1",
  image: "image.jpg",
  isLiked: false,
  likesCount: 1000,
  name: "naso337",
  text: "Some post text",
  time: "Mar 6, 2023, 09:58:32 PM"
}

const postReturnData = {
  postId: 3,
  postTime: "Mar 12, 2024, 09:58:32 AM"
}

function mockCallGet(mockProfileData, mockPostData) {
  axios.get.mockResolvedValueOnce({
    data: {
      state: "Success",
      message: "",
      data: mockProfileData
    }
  }).mockResolvedValueOnce({
    data: {
      state: "Success",
      message: "",
      data: mockPostData
    }
  });
}

function mockCallPost(mockData) {
  axios.post.mockResolvedValue({
    data: {
      state: "Success",
      message: "",
      data: mockData
    }
  });
}

jest.mock('../../components/modals/editUserModal.jsx', () => ({ isOpen }) => <p>editUserModal</p>);

test('Post Creation test', async () => {
  jest.spyOn(user, 'useUser').mockImplementation(() => {
    return {
      id: 1,
      identificator: "user1"
    }
  });

  mockCallGet(JSON.stringify(profileData), JSON.stringify(postData));

  await waitFor(() => {
    render(<UserProfile />);
  });

  await waitFor(() => {
    expect(screen.getByAltText("CreatePost")).toBeVisible();
  });

  userEvent.click(screen.getByAltText("CreatePost"));
  
  userEvent.type(screen.getByPlaceholderText('Введите текст'), "Random text");
  userEvent.selectOptions(screen.getByRole('combobox'), ['1']);

  mockCallPost(JSON.stringify(postReturnData));

  await waitFor(() => {
    userEvent.click(screen.getByRole('button', { name: 'Создать' }));
  });

  expect(screen.getByText("Наруто")).toBeVisible();
});
