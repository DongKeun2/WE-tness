import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import AccountCircle from '@mui/icons-material/AccountCircle';
import Swal from 'sweetalert2';
import withReactContent from 'sweetalert2-react-content';
import { signup, checkNickname, checkEmail, fetchNickname, fetchEmail } from '../../features/user/SignupSlice';
import PageBox from '../../components/common/auth/PageBox';
import FormBox from '../../components/common/auth/FormBox';
import InputBox from '../../components/common/auth/InputBox';
import SubmitBtn from '../../components/common/SubmitBtn';
import PasswordForm from '../../components/common/auth/PasswordForm';
import IconTextField from '../../components/common/IconTextField';
import CheckBtn from '../../components/common/CheckBtn';
import logo from '../../assets/images/logo.jpg';

const MySwal = withReactContent(Swal);

const SignupForm = styled.form`
  display: flex;
  flex-direction: column;
  margin-top: 100px;
  padding: 10px;
  gap: 15px;
`;

export default function SignupPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const userInfo = useSelector(state => state.signup.userInfo);
  const isPossibleNickname = useSelector(state => state.signup.isPossibleNickname);
  const isPossibleEmail = useSelector(state => state.signup.isPossibleEmail);
  const [isSignupError, setIsSignupError] = useState(false);
  const errorMsg = useSelector(state => state.signup.errorMsg);

  const [isCheckNN, setIsCheckNN] = useState(false);
  const [isCheckEmail, setIsCheckEmail] = useState(false);

  const onNicknameHandler = e => {
    setIsCheckNN(false);
    dispatch(fetchNickname(e.target.value));
  };
  const onEmailHandler = e => {
    setIsCheckEmail(false);
    dispatch(fetchEmail(e.target.value));
  };

  function onCheckNicknameHandler(e) {
    e.preventDefault();
    const payload = userInfo.nickname;
    dispatch(checkNickname(payload)).then(() => {
      setIsCheckNN(true);
    });
  }

  function onCheckEmailHandler(e) {
    e.preventDefault();
    const payload = userInfo.email;
    dispatch(checkEmail(payload)).then(() => {
      setIsCheckEmail(true);
    });
  }

  function onSubmitHandler(e) {
    e.preventDefault();
    const payload = {
      email: userInfo.email,
      nickname: userInfo.nickname,
      password: userInfo.password,
    };
    dispatch(signup(payload))
      .then(res => {
        if (res.type === 'signup/fulfilled') {
          MySwal.fire({
            title: <p>???????????????!</p>,
            text: '????????? ??? ???????????? ????????????!',
            icon: 'success',
          });
          navigate('/login');
        } else {
          setIsSignupError(true);
        }
      })
      .catch(err => {});
  }

  return (
    <PageBox>
      <FormBox>
        <SignupForm onSubmit={onSubmitHandler}>
          <h1>????????????</h1>
          <InputBox>
            <IconTextField
              error={isCheckNN && !isPossibleNickname}
              iconStart={<AccountCircle />}
              iconEnd={
                userInfo.nickname ? (
                  <CheckBtn onClick={onCheckNicknameHandler}>??????</CheckBtn>
                ) : (
                  <CheckBtn disabled deactive={!userInfo.nickname}>
                    ??????
                  </CheckBtn>
                )
              }
              label="*?????????"
              value={userInfo.nickname}
              onChange={onNicknameHandler}
              helperText={
                isCheckNN ? (isPossibleNickname ? '?????? ????????? ??????????????????.' : '???????????? ??????????????????.') : null
              }
            />
          </InputBox>

          <InputBox>
            <IconTextField
              error={isCheckEmail && !isPossibleEmail}
              iconEnd={
                userInfo.email ? (
                  <CheckBtn onClick={onCheckEmailHandler}>??????</CheckBtn>
                ) : (
                  <CheckBtn disabled deactive={!userInfo.email}>
                    ??????
                  </CheckBtn>
                )
              }
              type="email"
              label="*?????????"
              value={userInfo.email}
              onChange={onEmailHandler}
              helperText={
                isCheckEmail ? (
                  isPossibleEmail ? (
                    '?????? ????????? ??????????????????.'
                  ) : errorMsg ? (
                    <p>{errorMsg}</p>
                  ) : (
                    '???????????? ??????????????????.'
                  )
                ) : null
              }
            />
          </InputBox>

          <PasswordForm isError={isSignupError}></PasswordForm>

          <SubmitBtn
            disabled={
              !isCheckNN ||
              !isPossibleNickname ||
              !isCheckEmail ||
              !isPossibleEmail ||
              userInfo.password !== userInfo.pwdVerify ||
              userInfo.password === ''
            }
            deactive={
              !isCheckNN ||
              !isPossibleNickname ||
              !isCheckEmail ||
              !isPossibleEmail ||
              userInfo.password !== userInfo.pwdVerify ||
              userInfo.password === ''
            }>
            ????????????
          </SubmitBtn>
        </SignupForm>
        <p>
          ??????????????????? <Link to="/login">?????????</Link>{' '}
        </p>
      </FormBox>
      <FormBox>
        <img src={logo} alt="???????????????" />
      </FormBox>
    </PageBox>
  );
}
