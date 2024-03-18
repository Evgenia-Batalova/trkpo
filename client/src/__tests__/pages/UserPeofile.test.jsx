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

function mockCallGet(mockData) {
  axios.get.mockResolvedValue({
    data: {
      state: "Success",
      message: "",
      data: mockData
    }
  });
}

jest.mock('../../components/PostsList.jsx', () => () => <></>);
jest.mock('../../components/modals/createPostModal.jsx', () => ({ isOpen }) => <p>createPostModal</p>);
jest.mock('../../components/modals/editUserModal.jsx', () => ({ isOpen }) => <p>editUserModal</p>);

describe('UserProfile tests', () => {
  beforeEach(() => {
    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {
        id: 1
      }
    });
  });

  afterEach(() => {
    axios.get.mockClear();
  });

  test('UserProfile Render', async () => {
    mockCallGet(JSON.stringify(profileData));

    render(<UserProfile />);

    await waitFor(() => {
      expect(screen.getByText("Some profile info")).toBeVisible();
    });
  });

  test('UserProfile modals open', async () => {
    mockCallGet(JSON.stringify(profileData));

    await waitFor(() => {
      render(<UserProfile />);
    });

    await waitFor(() => {
      expect(screen.getByAltText("Change")).toBeVisible();
      expect(screen.getByAltText("CreatePost")).toBeVisible();
    });

    userEvent.click(screen.getByAltText("Change"));
    userEvent.click(screen.getByAltText("CreatePost"));

    await waitFor(() => {
      expect(screen.getByText("editUserModal")).toBeVisible();
      expect(screen.getByText("createPostModal")).toBeVisible();
    });
  });
});
