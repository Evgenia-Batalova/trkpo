import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import axios from "axios";
import PostsList from "../../components/PostsList";

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

const newPostData = {
  authorId: 2,
  authorImage: "image.jpg",
  category: "Наука",
  id: 2,
  identificator: "user1",
  image: "image.jpg",
  isLiked: false,
  likesCount: 1000,
  name: "naso1337",
  text: "Some post text",
  time: "Mar 6, 2023, 09:58:32 PM"
}

jest.mock('../../components/Post', () => ({ data }) => <p>{data.name}</p>);

function mockCallGet(mockData) {
  axios.get.mockResolvedValue({
    data: {
      state: "Success",
      message: "",
      data: mockData
    }
  });
}

describe('PostList tests', () => {
  afterEach(() => {
    axios.get.mockClear();
  });

  test('PostList Render', async () => {
    mockCallGet(JSON.stringify([postData]));

    render(<PostsList queryString={""} category={"It"} />);

    await waitFor(() => {
      expect(screen.getByText("naso337")).toBeVisible();
    });
  });

  test('PostList Render with newPost', async () => {
    mockCallGet(JSON.stringify([postData]));

    const { rerender } =  render(<PostsList queryString={""} />);

    await waitFor(() => {
      expect(screen.getByText("naso337")).toBeVisible();
    });

    rerender(<PostsList queryString={""} newPost={newPostData} />);

    await waitFor(() => {
      expect(screen.getByText("naso1337")).toBeVisible();
    });
  });

  test('PostList with zero elements', async () => {
    mockCallGet(JSON.stringify([]));

    render(<PostsList />);

    await waitFor(() => {
      expect(screen.getByText("В этой ленте ещё нет постов")).toBeVisible();
    });
  });

});
