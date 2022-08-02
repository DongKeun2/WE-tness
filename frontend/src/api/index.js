export const API_BASE_URL = 'http://localhost:8080';

const USER_URL = '/user';

const LOGIN_URL = '/login';
const KAKAO_URL = '/kakao';
const LOGOUT_URL = '/logout';
const SIGNUP_URL = '/join';
const EDIT_URL = '';
const CHECK_NICKNAME_URL = '/duplicate-nickname';
const CHECK_EMAIL_URL = '/duplicate-email';
const FETCH_FOLLOW_LIST_URL = '';
const CHANGE_PASSWORD = '';
const FIND_PASSWORD = '/findpassword';

export default {
  checkNickname: nickname => API_BASE_URL + USER_URL + CHECK_NICKNAME_URL + `/${nickname}`,
  checkEmail: email => API_BASE_URL + USER_URL + CHECK_EMAIL_URL + `/${email}`,
  signup: () => API_BASE_URL + USER_URL + SIGNUP_URL,
  login: () => API_BASE_URL + USER_URL + LOGIN_URL,
  logout: () => API_BASE_URL + USER_URL + LOGOUT_URL,
  kakao: () => API_BASE_URL + USER_URL + LOGIN_URL + KAKAO_URL,
  edit: () => API_BASE_URL + EDIT_URL,
  fetchFollowList: () => API_BASE_URL + FETCH_FOLLOW_LIST_URL,
  changePassword: () => API_BASE_URL + CHANGE_PASSWORD,
  findPassword: () => API_BASE_URL + FIND_PASSWORD,
};