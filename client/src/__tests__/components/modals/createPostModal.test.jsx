import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import axios from "axios";
import CreatePostModal from "../../../components/modals/createPostModal";
import * as user from "../../../components/utilities/userContext";

const inputString = "naso@()*&137nasonasoaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

function mockCallPost(data) {
  axios.post.mockResolvedValue({
    data: data
  });
}

describe('CreatePostModal form tests', () => {
  afterEach(() => {
    axios.post.mockClear();
  });

  test('Check modal is not on the page', () => {
    render(<CreatePostModal isOpen={false} />);
 
    expect(screen.queryByAltText("Close")).toBeNull();
  })

  test('Check modal is on the page', () => {
    render(<CreatePostModal isOpen={true} />);
 
    expect(screen.getByAltText("Close")).toBeInTheDocument();
  })

  test('Check button on disability', () => {
    render(<CreatePostModal isOpen={true} />);
   
    const button = screen.getByRole('button', { name: 'Создать'});
   
    expect(button.disabled).toBe(true);
  });

  test('Check button on availability', () => {
    render(<CreatePostModal isOpen={true} />);

    const button = screen.getByRole('button', { name: 'Создать'});
    const postText = screen.getByPlaceholderText('Введите текст');
    const select = screen.getByRole('combobox');
   
    expect(button.disabled).toBe(true);

    userEvent.type(postText, inputString);

    expect(button.disabled).toBe(true);

    userEvent.selectOptions(select, ['1']);
   
    expect(button.disabled).toBe(false);
  });

  test('Check select', () => {
    render(<CreatePostModal isOpen={true} />);

    const select = screen.getByRole('combobox');

    userEvent.selectOptions(select, ['1']);
    userEvent.selectOptions(select, ['0']);

    expect(screen.getByRole('option', {name: 'Наруто'}).selected).toBe(true);

    userEvent.selectOptions(select, ['2']);
    expect(screen.getByRole('option', {name: 'Блич'}).selected).toBe(true);
  });

  test('Post creation success', async () => {
    const data = JSON.stringify({
      postId: 1,
      postTime: ""
    });
    const returnValue = {
      state: "Success",
      message: "",
      data: data
    };
    const toggle = jest.fn();
    const setNewPost = jest.fn();

    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return { id: 1 }
    });

    render(<CreatePostModal isOpen={true} toggle={toggle} setNewPost={setNewPost} />);

    mockCallPost(returnValue);

    const select = screen.getByRole('combobox');
    const postText = screen.getByPlaceholderText('Введите текст');
    const createButton = screen.getByRole('button', { name: 'Создать'});

    userEvent.selectOptions(select, ['1']);
    userEvent.type(postText, inputString);

    await waitFor(() => {
      userEvent.click(createButton);
    });

    expect(toggle).toHaveBeenCalledTimes(1);
  });
});
