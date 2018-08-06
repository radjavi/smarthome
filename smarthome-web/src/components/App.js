import React, { Component } from 'react';
import {
  BrowserRouter as Router,
  Route,
  Redirect,
} from 'react-router-dom';

import SignInPage from './SignIn';
import DashboardPage from './Dashboard';
import { auth } from '../firebase';
import { firebase } from '../firebase';
import { Spin } from 'antd';

import './App.css';

class App extends Component {
  constructor(props) {
    super(props);

    this.state = {
      authUser: null,
      loading: true,
    };
  }

  componentDidMount() {
    firebase.auth.onAuthStateChanged(authUser => {
      authUser
        ? this.setState({ authUser, loading: false })
        : this.setState({ authUser: null, loading: false });
    });
  }

  render () {
    if (this.state.loading) return (
      <div id="spinner">
        <Spin size="large"/>
      </div>
    );

    if (this.state.authUser != null) {
      return <DashboardPage authUser={this.state.authUser} />
    }
    else {
      return <SignInPage authUser={this.state.authUser} />
    }
  }
}

export default App;
