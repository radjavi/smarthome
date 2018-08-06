import React from 'react';
import { auth } from '../firebase';
import { firebase } from '../firebase';
import {
  BrowserRouter as Router,
  Route,
  Redirect,
} from 'react-router-dom';
import { Form, Icon, Input, Button, Spin, Alert, message } from 'antd';
import DashboardPage from './Dashboard';

const FormItem = Form.Item;
class NormalLoginForm extends React.Component {
  state = {
    loading: false,
    error: null,
  }

  handleSubmit = (event) => {
    event.preventDefault();

    this.props.form.validateFields((err, values) => {
      if (!err) {
        this.setState({ loading: true })
        auth.doSignInWithEmailAndPassword(values.email, values.password)
          .then(() => {
            this.setState(() => ({
                loading: false,
              }));
            message.success("Successfully signed in!");
          })
          .catch(error => {
            this.setState(() => ({
                loading: false,
                error: error
              }));
            message.error(error.message);
          });
      }
    });
  }

  render() {
    const { getFieldDecorator } = this.props.form;
    const error = this.state.error;
    return (
      <div id="signIn">
        <img id="houseLogo" src={require('./house-blue.png')} height="60" />
        <Form onSubmit={this.handleSubmit} className="login-form">
          <FormItem>
            {getFieldDecorator('email', {
              rules: [{
              type: 'email', message: 'The input is not a valid email!',
              }, {
                required: true, message: 'Please input your email!',
              }],
            })(
              <Input
                prefix={<Icon type="user"
                style={{ color: 'rgba(0,0,0,.25)' }} />}
                size="large"
                placeholder="Email"
              />
            )}
          </FormItem>
          <FormItem>
            {getFieldDecorator('password', {
              rules: [{ required: true, message: 'Password is required!' }],
            })(
              <Input
                prefix={<Icon type="lock"
                style={{ color: 'rgba(0,0,0,.25)' }} />}
                size="large"
                type="password" placeholder="Password"
              />
            )}
          </FormItem>
          <FormItem>
            <Button type="primary" size="large" htmlType="submit" className="login-form-button" loading={this.state.loading}>
              Sign in
            </Button>
          </FormItem>
        </Form>
      </div>
    );
  }
}

const SignInPage = Form.create()(NormalLoginForm);

export default SignInPage;
