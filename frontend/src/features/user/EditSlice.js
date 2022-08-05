import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';
import api from '../../api/index';
import setConfig from '../authHeader';
import {
  setAccessToken,
  removeAccessToken,
  setRefreshToken,
  removeRefreshToken,
  decodeAccessToken,
  setCurrentUser,
  removeCurrentUser,
  getCurrentUser,
} from '../Token';

const fetchUserInfo = createAsyncThunk('fetchUserInfo', async (arg, { rejectWithValue }) => {
  try {
    const res = await axios.get(api.fetchUserInfo(), setConfig());
    console.log(res);
    return res.data;
  } catch (err) {
    return rejectWithValue(err.response);
  }
});

const edit = createAsyncThunk('edit', async (payload, { rejectWithValue }) => {
  try {
    const res = await axios.put(api.edit(), payload, setConfig());
    console.log(res.data);
    removeAccessToken();
    removeRefreshToken();
    removeCurrentUser();
    setAccessToken(res.data.accessToken);
    setRefreshToken(res.data.refreshToken);
    setCurrentUser(decodeAccessToken(res.data.accessToken));
    return res.data;
  } catch (err) {
    return rejectWithValue(err.response);
  }
});

const changePassword = createAsyncThunk('changePassword', async (payload, { rejectWithValue }) => {
  try {
    const res = await axios.patch(api.changePassword(), payload, setConfig());
    return res;
  } catch (err) {
    return rejectWithValue(err.response);
  }
});

const initialState = {
  userInfo: {
    nickname: '',
    email: '',
    gender: '',
    address: '',
    weight: '',
    height: '',
  },
  addressCode: '',
};

export const SignupSlice = createSlice({
  name: 'edit',
  initialState,
  reducers: {
    fetchNickname: (state, action) => {
      state.userInfo.nickname = action.payload;
    },
    fetchEmail: (state, action) => {
      state.userInfo.email = action.payload;
    },
    fetchPassword: (state, action) => {
      state.userInfo.password = action.payload;
    },
    fetchPwdVerify: (state, action) => {
      state.userInfo.pwdVerify = action.payload;
    },
    fetchGender: (state, action) => {
      state.userInfo.gender = action.payload;
    },
    fetchAddress: (state, action) => {
      state.userInfo.address = action.payload;
    },
    fetchAddressCode: (state, action) => {
      state.addressCode = action.payload;
    },
    fetchHeight: (state, action) => {
      state.userInfo.height = action.payload;
    },
    fetchWeight: (state, action) => {
      state.userInfo.weight = action.payload;
    },
  },
  extraReducers: {
    [edit.pending]: state => {
      state.isLoading = true;
    },
    [edit.fulfilled]: (state, action) => {
      state.isLoading = false;
    },
    [edit.rejected]: state => {
      state.isLoading = false;
    },
    [fetchUserInfo.fulfilled]: (state, action) => {
      state.userInfo = action.payload;
    },
  },
});

export { fetchUserInfo, edit, changePassword };

export const {
  fetchNickname,
  fetchEmail,
  fetchPassword,
  fetchPwdVerify,
  fetchGender,
  fetchAddress,
  fetchAddressCode,
  fetchHeight,
  fetchWeight,
} = SignupSlice.actions;

export default SignupSlice.reducer;