import React from 'react';
import { Button, Form, Icon, Card, Layout, Menu, message, Dropdown, Row, Col, Switch, Divider } from 'antd';
import { auth } from '../firebase';
import firebase from 'firebase';
import {
  BrowserRouter as Router,
  Route,
  Redirect,
} from 'react-router-dom';

const { Header, Content, Footer } = Layout;
const db = firebase.database();
const tempSensorRef = db.ref("tempSensor");
const alarmRef = db.ref("alarm");
const ledRef = db.ref("led");
const piOnlineRef = db.ref("piOnline");

class DashboardPage extends React.Component {

  constructor() {
    super();
    this.state = {
      temp: 0,
      humid: 0,
      date: '-',
      maxTemp: 0,
      minTemp: 0,
      maxHumid: 0,
      minHumid: 0,
      alarm: false,
      ledOn: false,
    };
  }

  componentDidMount() {
    tempSensorRef.on('value', snapshot => {
      const parent = snapshot.val();
      this.setState({
        temp: parent.temp,
        humid: parent.humid,
        date: parent.date,
        maxTemp: parent.maxTemp,
        minTemp: parent.minTemp,
        maxHumid: parent.maxHumid,
        minHumid: parent.minHumid,
      });
    });
    alarmRef.on('value', snapshot => {
      this.setState({
        alarm: snapshot.val(),
      });
    });
    ledRef.on('value', snapshot => {
      this.setState({
        ledOn: snapshot.val().ledOn,
      });
    });
  }

  render() {
    function onSignOut() {
      auth.doSignOut()
        .then(() => {
          message.success("Successfully signed out!");
        })
        .catch(error => {
          message.error("Error occured!");
        });
    };

    function alarmChange(checked) {
      alarmRef.set(checked);
    }

    function ledOnChange(checked) {
      const rgbCode = checked ? "BTN_ON" : "BTN_OFF";
      ledRef.set({
        ledOn: checked,
        rgbCode: rgbCode,
      });
    }

    const onClick = function ({ key }) {
      switch(key) {
        case "1":
          message.info("Settings");
          break;
        case "2":
          onSignOut();
          break;
      }
    };

    const menu = (
      <Menu onClick={onClick}>
        <Menu.Item key="1"><Icon type="setting" /> Settings</Menu.Item>
        <Menu.Item key="2"><Icon type="logout" /> Sign out</Menu.Item>
      </Menu>
    );

    const user = firebase.auth().currentUser;
    const displayName = user.displayName ? user.displayName : "Profile";

    return (
      <Layout className="layout">

        <div className="header">
          <div className="logo"><h2>Dashboard</h2></div>
          <Menu
            theme="light"
            mode="horizontal"
            defaultSelectedKeys={['1']}
            style={{ lineHeight: '64px' }}
          >
            <Menu.Item key="1">Home</Menu.Item>
            <div className="dropProfile">
              <Dropdown overlay={menu}>
                <Button>
                  <Icon type="user" /> {displayName}
                </Button>
              </Dropdown>
            </div>
          </Menu>
        </div>

        <Content className="content">
          <p><Icon type="sync" />&nbsp;&nbsp;&nbsp;{this.state.date}</p>
          <Divider type="horizontal" />
          <Row gutter={16}>

            <Col span={8}>
              <Card title="Temperature" type="inner">
                <Row>
                  <Col span={12}>
                    <h2 className="temp">{this.state.temp}°C</h2>
                  </Col>
                  <Col span={12}>
                    <h2 className="temp">{this.state.humid}%</h2>
                  </Col>
                </Row>

                <Row>
                  <Col span={12}>
                    <p className="temp">{this.state.maxTemp}° / {this.state.minTemp}°</p>
                  </Col>
                  <Col span={12}>
                    <p className="temp">{this.state.maxHumid}% / {this.state.minHumid}%</p>
                  </Col>
                </Row>

              </Card>
            </Col>

            <Col span={8}>
              <Card title="Alarm" type="inner" extra={<Switch checked={this.state.alarm} onChange={alarmChange} />}>Alarm is disarmed.</Card>
            </Col>

            <Col span={8}>
              <Card title="Desk Lamp" type="inner" extra={<Switch checked={this.state.ledOn} onChange={ledOnChange} />}>Desk Lamp is off.</Card>
            </Col>

          </Row>
        </Content>

      </Layout>
    );
  }
}

export default DashboardPage;
