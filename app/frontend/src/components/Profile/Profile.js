import React, {Component} from 'react';
import {Button, Icon} from 'semantic-ui-react'
import {loadState} from '../../_core/localStorage'
import {Label, Grid, Segment, Header, Rating, List, Divider} from 'semantic-ui-react';
import {connect} from 'react-redux';
import history from '../../_core/history';
import * as userActions from '../../actions/userActions';
import {Card} from 'semantic-ui-react'
import moment from 'moment';
import ProfileCard from "./ProfileCard";
import {colorAccent, colorBG, colorPrimary} from "../../utils/constants/Colors";

class Profile extends Component {

    constructor(props) {
        super(props);
        this.state = {
            userLocal: {},
            index: 0,
            user: {following: 0, follower: 0},
            portfolios: [],
            tradingEqs: {}
        }
    }

    componentDidMount() {


        this.getProfile();
        this.state.portfolios.forEach(element => {
            console.log(element);
            this.getPortfolios(element._id);
        })

    }

    acceptFollow(id) {
        this.props.acceptFollow(id).then(() => {
            this.getProfile();
        })
    }

    rejectFollow(id) {
        this.props.rejectFollow(id).then(() => {
            this.getProfile();
        })
    }


    async getPortfolios(i) {
        await this.props.portfolios(this.state.portfolios[i]._id).then(async result => {
                let newPortfolios = result.value
                let newTradingEqs = {}
                console.log(newPortfolios.tradingEqs)
                newTradingEqs[i] = newPortfolios.tradingEqs
                this.setState({tradingEqs: newTradingEqs})

            }
        )
    }

    async getProfile() {

        await this.props.profile(loadState().user._id).then(async result => {
                console.log(result.value);
                let newProfile = result.value
                console.log(newProfile.portfolios)
                this.setState({user: newProfile})
            }
        )
    }


    render() {


        const {user, portfolios, userLocal, tradingEqs} = this.state;

        console.log(user);

        const profileCardProps = {...user.user, following: user.following, followers: user.follower};

        const ButtonExampleFloated = () => (
            <div>
                <Button active floated='right'>Right Floated</Button>

            </div>
        );
        return (

            <Grid>
                <Grid.Row>

                    <Grid.Column style={{marginLeft: 30}} width={3}>
                        <Grid.Row relaxed>
                            <ProfileCard user={profileCardProps} style={{background: "rgba(255,255,255,0.5)"}}/>
                        </Grid.Row>
                        {user.followRequests && user.followRequests.length > 0 &&
                        <Grid.Row relaxed>
                            <Segment textAlign="left" style={{
                                margin: 20,
                                width: "100%",
                                backgroundColor: "white",
                                borderColor: colorPrimary,
                                borderRadius: 20,
                                borderWidth: 1.5
                            }}>
                                <List divided relaxed textAlign="left">
                                    <List.Header style={{color: "#c9c9c9"}} as="h3">Pending Requests</List.Header>
                                    {user.followRequests.map(follower => {
                                        return <List.Item icon="user"
                                                          style={{color: "#c9c9c9", cursor: "pointer"}}
                                        >
                                            <div>
                                                <Icon name="user"/><span onClick={() => {
                                                history.push("/profile/" + follower.FollowingId)
                                            }}>{follower.FollowingName + " " + follower.FollowingSurname}</span>
                                                <Label circular content="Reject" basic color="red"
                                                       style={{float: "right"}}
                                                       onClick={this.rejectFollow.bind(this, follower._id)}/>
                                                <Label circular content="Accept" basic color="green"
                                                       style={{float: "right"}}
                                                       onClick={this.acceptFollow.bind(this, follower._id)}/>
                                            </div>
                                        </List.Item>
                                    })}
                                </List>
                            </Segment>
                        </Grid.Row>
                        }
                        <Grid.Row relaxed>
                            <Segment textAlign="left" style={{
                                margin: 20,
                                width: "100%",
                                background: colorBG,
                                borderColor: colorPrimary,
                                borderRadius: 20,
                                borderWidth: 1.5
                            }}>
                                <List animated divided relaxed textAlign="left">
                                    <List.Header as="h3"
                                                 style={{color: "#c9c9c9"}}>{user.follower + " Followers"}</List.Header>
                                    {user.followers && user.followers.map(follower => {
                                        return <List.Item icon="user"
                                                          style={{color: "#c9c9c9", cursor: "pointer"}}
                                                          onClick={() => {
                                                              history.push("/profile/" + follower.FollowingId)
                                                          }}
                                                          content={follower.FollowingName + " " + follower.FollowingSurname}/>
                                    })}
                                </List>
                            </Segment>
                        </Grid.Row>
                        <Grid.Row relaxed>
                            <Segment textAlign="left" style={{
                                margin: 20,
                                width: "100%",
                                background: colorBG,
                                borderColor: colorPrimary,
                                borderRadius: 20,
                                borderWidth: 1.5
                            }}>
                                <List animated divided relaxed textAlign="left">
                                    <List.Header as="h3"
                                                 style={{color: "#c9c9c9"}}>{user.following + " Following"}</List.Header>
                                    {user.followings && user.followings.map(follower => {
                                        return <List.Item icon="user"
                                                          style={{color: "#c9c9c9", cursor: "pointer"}}
                                                          onClick={() => {
                                                              history.push("/profile/" + follower.FollowedId)
                                                          }}
                                                          content={follower.FollowedName + " " + follower.FollowedSurname}/>
                                    })}
                                </List>
                            </Segment>
                        </Grid.Row>
                    </Grid.Column>
                    <Grid.Column width={8}>
                        <Segment style={{
                            margin: 20,
                            width: "100%",
                            background: colorBG,
                            borderColor: colorPrimary,
                            borderRadius: 20,
                            borderWidth: 1.5
                        }}>
                            <Header style={{color: "#c9c9c9"}}>Articles<Button style={{float: "right"}} basic
                                                                               color="blue" onClick={() => {
                                history.push("/articles/new")
                            }}>Add</Button></Header>
                            <Divider/>
                            {user.articles && user.articles.length > 0 ?
                                user.articles.map(article => {
                                    return (
                                        <Card style={{width: "100%", background: "rgba(255,255,255,0.15)"}}
                                              onClick={() => {
                                                  history.push("/articles/" + article._id)
                                              }}>
                                            <Card.Content>
                                                <Card.Header style={{color: "#c9c9c9"}}>{article.title}</Card.Header>
                                                <Card.Meta type="date"
                                                           style={{color: "#c9c9c9"}}>{moment(article.date).format("DD/MM/YYYY HH:mm")}</Card.Meta>
                                                <Card.Description
                                                    style={{color: "#c9c9c9"}}>{article.text.substring(0, 350) + "..."}</Card.Description>
                                            </Card.Content>
                                            <Card.Content style={{color: "#c9c9c9"}} extra>
                                                <Label style={{
                                                    fontSize: 14,
                                                    backgroundColor: colorAccent,
                                                    color: "white"
                                                }}>
                                                    <div style={{
                                                        display: "flex",
                                                        flexDirection: "row",
                                                        width: 25,
                                                        justifyContent: "center"
                                                    }}>
                                                        {(article.rateAverage ? article.rateAverage.toFixed(1) : 0)}
                                                    </div>
                                                </Label>{" by " + article.numberOfRates + " votes"}
                                            </Card.Content>
                                        </Card>
                                    )
                                }) : <span style={{color: "#c9c9c9"}}>No Article Created!</span>
                            }
                        </Segment>
                    </Grid.Column>
                    <Grid.Column width={4}>
                        <Grid.Row>
                            <Segment textAlign="left" style={{
                                margin: 20,
                                width: "100%",
                                background: colorBG,
                                borderColor: colorPrimary,
                                borderRadius: 20,
                                borderWidth: 1.5
                            }}>
                                <List animated divided relaxed textAlign="left">
                                    <List.Header as="h3" style={{color: "#c9c9c9"}}>Followed Trading
                                        Equipment</List.Header>
                                    {user.followingTradings && user.followingTradings.length > 0 ? user.followingTradings.map(teq => {
                                        return <List.Item icon="chart line"
                                                          style={{color: "#c9c9c9", cursor: "pointer"}}
                                                          onClick={() => {
                                                              history.push({
                                                                  pathname: "trading-equipment",
                                                                  state: {currency: teq.TradingEq}
                                                              })
                                                          }}

                                                          content={teq.TradingEq === "EUR" ? (teq.TradingEq + "/USD") : (teq.TradingEq + "/EUR")}/>
                                    }) : <List.Item style={{color: "#c9c9c9"}}
                                                    content="No Trading Equipment Is Followed"/>}

                                </List>
                            </Segment>
                        </Grid.Row>
                        <Grid.Row>
                            <Segment style={{
                                margin: 20,
                                width: "100%",
                                background: colorBG,
                                borderColor: colorPrimary,
                                borderRadius: 20,
                                borderWidth: 1.5
                            }}>
                                <Header style={{color: "#c9c9c9"}}>Portfolios<Button style={{float: "right"}} basic
                                                                                     color="blue" onClick={() => {
                                    history.push("/portfolios/new")
                                }}>Add</Button></Header>
                                <Divider style={{color: "#c9c9c9"}}/>
                                {user.portfolios && user.portfolios.length > 0 ?
                                    user.portfolios.map(portfolio => {
                                        return (
                                            <Card style={{background: "rgba(255,255,255,0.15)", width: "100%"}}
                                                  onClick={() => history.push("/portfolios/" + portfolio._id)}>
                                                <Card.Content>
                                                    <Card.Header
                                                        style={{color: "#c9c9c9"}}>{portfolio.title}</Card.Header>
                                                    <Card.Description
                                                        style={{color: "#c9c9c9"}}>{portfolio.definition}</Card.Description>
                                                </Card.Content>

                                            </Card>
                                        )
                                    }) : <span style={{color: "#c9c9c9"}}>No Portfolio Created!</span>
                                }
                            </Segment>
                        </Grid.Row>
                        <Grid.Row>
                            <Segment style={{
                                margin: 20,
                                width: "100%",
                                background: colorBG,
                                borderColor: colorPrimary,
                                borderRadius: 20,
                                borderWidth: 1.5
                            }}>
                                <Header style={{color: "#c9c9c9"}}>Followed Portfolios</Header>
                                <Divider style={{color: "#c9c9c9"}}/>
                                {user.followingPortfolios && user.followingPortfolios.length > 0 ?
                                    user.followingPortfolios.map(portfolio => {
                                        return (
                                            <Card style={{background: "rgba(255,255,255,0.15)", width: "100%"}}
                                                  onClick={() => {
                                                      history.push("/portfolios/" + portfolio._id)
                                                  }}>
                                                <Card.Content>
                                                    <Card.Header style={{color: "#c9c9c9"}}>{portfolio.title} <span
                                                        style={{fontSize: 13}}> by <u>{portfolio.username + " " + portfolio.surname}</u> </span></Card.Header>
                                                    <Card.Description
                                                        style={{color: "#c9c9c9"}}>{portfolio.definition}</Card.Description>
                                                </Card.Content>

                                            </Card>
                                        )
                                    }) : <span style={{color: "#c9c9c9"}}>No Portfolio Followed!</span>
                                }
                            </Segment>
                        </Grid.Row>
                    </Grid.Column>
                </Grid.Row>
            </Grid>
        )
    }
}

const dispatchToProps = dispatch => {
    return {
        profile: params => dispatch(userActions.profile(params)),
        portfolios: params => dispatch(userActions.portfolios(params)),
        acceptFollow: params => dispatch(userActions.acceptFollow(params)),
        rejectFollow: params => dispatch(userActions.rejectFollow(params))

    };
};

export default connect(null, dispatchToProps)(Profile);
