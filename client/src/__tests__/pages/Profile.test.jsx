import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import axios from "axios";
import Profile from "../../pages/Profile";
import * as user from "../../components/utilities/userContext";

const profileData = JSON.stringify({
  name: "naso337",
  identificator: "user1",
  imagePath: "image.jpg",
  follows: 3,
  followers: 5,
  info: "Some profile info"
});

const profileDataSub = JSON.stringify({
  name: "naso337",
  identificator: "user1",
  imagePath: "image.jpg",
  follows: 3,
  followers: 6,
  info: "Some profile info"
});

const baseSrc = "http://localhost/";

function mockCallGet() {
  axios.get.mockResolvedValueOnce({
    data: {
      state: "Success",
      message: "",
      data: 1
    }
  }).mockResolvedValueOnce({
    data: {
      state: "Success",
      message: "",
      data: profileData
    }
  }).mockResolvedValueOnce({
    data: {
      state: "Success",
      message: "",
      data: false
    }
  });
}

function mockCallPost(mockData) {
  axios.post.mockResolvedValue({
    data: mockData
  });
}

function mockCallDelete(mockData) {
  axios.delete.mockResolvedValue({
    data: mockData
  });
}

jest.mock('../../components/PostsList.jsx', () => () => <></>);

const mockedUseParams = jest.fn().mockReturnValue({ identifier: "user1" });

jest.mock('react-router-dom', () => ({
  useParams: () => mockedUseParams,
}));

describe('UserProfile tests', () => {
  const setErrorMessage = jest.fn();
  const toggleError = jest.fn();

  beforeEach(() => {
    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {
        id: 1,
        setErrorMessage,
        toggleError
      }
    });
  });

  afterEach(() => {
    axios.get.mockClear();
    axios.put.mockClear();
    axios.delete.mockClear();
  });

  test('Profile Render', async () => {
    mockCallGet();

    await waitFor(() => {
      render(<Profile />);
    });

    await waitFor(() => {
      expect(screen.getByText("Some profile info")).toBeVisible();
    });
  });

  test('Profile subscription', async () => {
    const data = {
      state: "Success",
      message: "",
      data: {}
    }

    mockCallGet();

    await waitFor(() => {
      render(<Profile />);
    });

    await waitFor(() => {
      expect(screen.getByText("Подписчики: 5")).toBeInTheDocument();
    });

    mockCallPost(data);
    userEvent.click(screen.getByAltText("Subscribe"));

    await waitFor(() => {
      expect(screen.getByText("Подписчики: 6")).toBeInTheDocument();
    });

    mockCallDelete(data);
    userEvent.click(screen.getByAltText("Subscribe"));

    await waitFor(() => {
      expect(screen.getByText("Подписчики: 5")).toBeInTheDocument();
    });
  });

  test('Profile subscription errors', async () => {
    const data = {
      state: "Success",
      message: "",
      data: {}
    }

    const dataError = {
      state: "Error",
      message: "",
      data: {}
    }

    mockCallGet();

    await waitFor(() => {
      render(<Profile />);
    });

    await waitFor(() => {
      expect(screen.getByText("Подписчики: 5")).toBeInTheDocument();
    });

    mockCallPost(dataError);
    userEvent.click(screen.getByAltText("Subscribe"));

    await waitFor(() => {
      expect(setErrorMessage).toHaveBeenCalledTimes(1);
      expect(toggleError).toHaveBeenCalledTimes(1);
    });

    mockCallPost(data);
    userEvent.click(screen.getByAltText("Subscribe"));

    await waitFor(() => {
      expect(screen.getByText("Подписчики: 6")).toBeInTheDocument();
    });

    mockCallDelete(dataError);
    userEvent.click(screen.getByAltText("Subscribe"));

    await waitFor(() => {
      expect(setErrorMessage).toHaveBeenCalledTimes(2);
      expect(toggleError).toHaveBeenCalledTimes(2);
    });
  });
});
