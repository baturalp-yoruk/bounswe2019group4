const bcrypt = require('bcryptjs');
let { User } = require('./../models/user.js');  // The connection to the User model in the database
let { Notification } = require('./../models/notification.js'); 

const {findUserFollows, checkPassword, checkIBAN, checkTCKN} = require('../utils')
/*
  Get method for profile page.
  Get user id from parameter and responses accordingly.
*/

async function profileResponse(user, me, followStatus, TradingEqFollow, Article, Portfolio, PortfolioTradingEq, PortfolioFollow, User) {
  let followings = await findUserFollows({ FollowingId: user._id, status: true })
  let followers = await findUserFollows({ FollowedId: user._id, status: true })
  let followRequests = await findUserFollows({ FollowedId: user._id, status: false})
  let followingTradings = await TradingEqFollow.find({ UserId : user._id })
  let followingPortfoliosTemp = await PortfolioFollow.find({ UserId : user._id })
  let articles = await Article.find().where('userId').equals(user._id)
  let portfoliosBefore = null
  let followingPortfolios = []
  let portfolios = []
  if(me){
    portfoliosBefore = await Portfolio.find().where('userId').equals(user._id)   
  }
  else{
    portfoliosBefore = await Portfolio.find({userId: user._id, isPrivate: false})
  }

  for (const portfolio of portfoliosBefore) {
    let tradingEqsObj = await PortfolioTradingEq.find({ PortfolioId : portfolio._id})
    let tradingEqs = []
    for (const tradingEq of tradingEqsObj) {
      tradingEqs.push(tradingEq['TradingEq']);
    }
    obj = {
      ...portfolio["_doc"],
      tradingEqs: tradingEqs
    }
    portfolios.push(obj)
  }

  for(const p of followingPortfoliosTemp){
    let temp = await Portfolio.find({ _id : p['PortfolioId']})
    let user = await User.find({_id : temp[0]['userId']})
    obj = {
      ...temp[0]["_doc"],
      userId: user[0]['_id'],
      username: user[0]['name'],
      surname: user[0]['surname'],
      PortfolioId: p['PortfolioId']
    }
    followingPortfolios.push(obj)
  }

  if(me){ // if profile is mine
    obj = {
      user,
      following: followings.length,
      followings,
      follower: followers.length,
      followers,
      followRequest: followRequests.length,
      followRequests,
      followingTradings,
      articles,
      portfolios,
      followingPortfolios
    }
    return obj;
  } else { // if profile is others
      if (user.isPublic || followStatus == 'TRUE') { // if profile is public or i am following
        tempUser = { _id : user._id, 
          isTrader : user.isTrader, 
          isPublic : user.isPublic, 
          name : user.name, 
          surname : user.surname, 
          email : user.email, 
          location: user.location,
          predictionRate: user.predictionRate,
          followingPortfolios
        }

        obj = {
          user : tempUser,
          following: followings.length,
          followings,
          follower: followers.length,
          followers,
          followStatus,
          followingTradings,
          articles,
          portfolios,
          followingPortfolios
      }
      return obj;
      } else { // if profile is private and i am not following right now.
        tempUser = { _id : user._id, 
          isPublic : user.isPublic, 
          name : user.name, 
          surname : user.surname
        }

        obj = {
          user: tempUser,
          following: followings.length,
          follower: followers.length,
          articles,
          followStatus
        }
        return obj
    }
  }
}


module.exports.getDetails = async (request, response) => {
  let UserFollow = request.models['UserFollow']
  let User = request.models['User']
  let TradingEqFollow = request.models['TradingEquipmentFollow']
  let Article = request.models['Article']
  let Portfolio = request.models['Portfolio']
  let PortfolioFollow = request.models['PortfolioFollow']
  let PortfolioTradingEq = request.models['PortfolioTradingEq']

  const requestedUserId = request.params['id']
  const currentUser = request.session['user']

  try {
    if(currentUser && currentUser._id == requestedUserId) {  // when the user asks for his own details
      requestedUser = await User.findOne({ _id : requestedUserId })
      res = await profileResponse(requestedUser, true, null, TradingEqFollow, Article, Portfolio, PortfolioTradingEq, PortfolioFollow, User)
      return response.send(res);
    } else {  // when the user requested isn't the user logged in himself
      const requestedUser = await User.findOne({ _id : requestedUserId })   // finds the user instance requested if it exists
      if(requestedUser){ // if it exists
        followStatus = 'FALSE'
        if(currentUser){ // if user logged in
          entry = await UserFollow.findOne({ FollowingId : request.session['user']._id, FollowedId : requestedUserId }) // check whether they follow each other
          if(entry){ // if following or request sent
            followStatus = entry.status ? 'TRUE' : 'PENDING' 
          } else {
            followStatus = 'FALSE'
          }
        }
        res = await profileResponse(requestedUser, false, followStatus, TradingEqFollow, Article, Portfolio, PortfolioTradingEq, PortfolioFollow, User)
        return response.send(res);        
      } else {  // when there's no user with given ID
        return response.status(400).send({
          errmsg: "No such user."
        })
      }
    }
  } catch (error) {
    return response.status(400).send({errmsg: 'Unexpected error occured. Please check the request context and try again.'})
  }
}

/*
  Get method for following user.
*/
module.exports.followUser = async (request, response) => {
  const UserFollow = request.models['UserFollow']
  const User = request.models['User']
  
  let followingId = request.session['user']._id
  let followedId = request.params['id']

  follower = await User.findOne({ _id : followedId })

  if(follower){ // If user exists
    status = await UserFollow.findOne({ FollowingId : followingId, FollowedId : followedId })

    if(!status){ // If not following right now
      let follow = new UserFollow({
        FollowingId: followingId,
        FollowedId: followedId,
      });

      if(follower.isPublic){ // If user is public
        follow.status = true
      } else {
        follow.status = false
      }

    follow.save()
      .then(async doc => {
        let text = request.session['user'].name + " " + request.session['user'].surname + " followed you."

        if(follow.status == false)
          text = request.session['user'].name + " " + request.session['user'].surname + " wants to follow you."

        let notification = new Notification({
          userId: followedId,
          text: text,
          date: new Date()
        })
      
        await notification.save()
        
        return response.status(204).send();
      }).catch(error => {
        return response.status(400).send(error);
      });
    } else {
        return response.status(400).send({
          errmsg: "Already following or requested to follow that user."
        })
    }
  } else {
      return response.status(400).send({
      errmsg: "No such user."
    })
  }
}

/*
  Get method for unfollowing user.
*/
module.exports.unfollowUser = async (request, response) => {
  let UserFollow = request.models['UserFollow']

  const followingId = request.session['user']._id
  const followedId = request.params['id']
  
  UserFollow.deleteOne({ FollowingId : followingId, FollowedId : followedId }, (err, results) => {
    if(err){
      return response.status(404).send({
        errmsg: "Failed."
      })
    }

    return response.sendStatus(204);
  });
}

/*
  Get method for accepting user follow request.
*/
module.exports.acceptRequest = async (request, response) => {
  let UserFollow = request.models['UserFollow']

  const followingId = request.session['user']._id
  const requestId = request.params['id']

  req = await UserFollow.findOne({ _id : requestId, FollowedId: request.session['user']._id, status: false})

  // If there exists such request
  if(req){ 
    req.status = true;

    // Save it into user-follow table
    req.save() 
      .then(async doc => {
        let notification = new Notification({
          userId: req.FollowingId,
          text: request.session['user'].name + " " + request.session['user'].surname + " has accepted your follow request.",
          date: new Date()
        })
      
        await notification.save()

        return response.status(204).send();
      }).catch(error => {
        return response.status(400).send(error);
      });    
  } else {
    response.status(400).send({
      errmsg: "No such request."
    })
  }
  
}

/*
  Get method for rejecting user follow request.
*/
module.exports.rejectRequest = async (request, response) => {
  const UserFollow = request.models['UserFollow']
  
  const requestId = request.params['id']

  await UserFollow.deleteOne({ _id : requestId, FollowedId: request.session['user']._id, status: false }, (err, results) => {
    if(err){
      return response.status(404).send({
        errmsg: "Failed."
      })
    }
    
    return response.sendStatus(204);
  })
}

/*
  Get method for canceling user follow request.
*/
module.exports.cancelRequest = async (request, response) => {
  const UserFollow = request.models['UserFollow']
  
  await UserFollow.deleteOne({ FollowedId : request.params['id'], FollowingId: request.session['user']._id, status: false }, (err, results) => {
    if(err){
      return response.status(404).send({
        errmsg: "Failed."
      })
    }
    
    return response.sendStatus(204);
  })
}

/*
  Patch method for editing profile details.
  It saves profile to database.
*/
module.exports.editProfile = async (request, response) => {
  const userId = request.session['user']._id

  const { name, surname, email, location, isTrader, isPublic, iban, tckn } = request.body  // Extract fields

  // check valid iban
  if(iban && !checkIBAN(iban)) {
    return response.status(400).send({
      errmsg: "Enter valid iban."
    })
  }
  // check valid tckn
  if(tckn && !checkTCKN(tckn)) {
    return response.status(400).send({
      errmsg: "Enter valid TCKN"
    })
  }

  row = await User.findOne({ _id : userId});

  if(row){
    // Check email is changed
    if(email && email != row.email){
      return response.status(400).send({
        errmsg: "Users cannot change their email."
      })
    }
    try{
        await User.updateOne({_id:userId},{ name: name, surname: surname, location: location, 
                                            iban: iban, tckn: tckn, isPublic: isPublic, isTrader: isTrader}) 
        .then(async  doc => {
          let edittedUser = await User.findOne({ _id : userId});
          request.session['user'] = edittedUser;
          return response.status(204).send();
        }).catch(error => {
          return response.status(400).send(error);
        });
    } catch(error){
      return response.status(404).send({
        errmsg: error.message
      })
    }
  } else {
    return response.status(400).send({
      errmsg: "No such user."
       })
  }
}

/*
  Patch method for change password.
*/
module.exports.changePassword = async (request, response) => {
  const userId = request.session['user']._id

  const { oldPassword, password } = request.body  // Extract fields

  row = await User.findOne({ _id : userId});

  if(row){
    // check whether user is authenticated via Google. If so, return error
    if(row.googleId){
      return response.status(400).send({
        errmsg: "Users authenticated via Google cannot change their password"
      })
    }

    // If password hashed in the user instance doesn't match with the old password in the request, credentials are invalid
    if (bcrypt.compareSync(oldPassword, row['password'])) {
       // The user credentials are correct
       // check valid password
      if(password && !checkPassword(password)) {
        return response.status(400).send({
          errmsg: "Enter valid password. Your password either is less than 6 characters or is easy to guess."
        })
      }
      
      // Hashes the password
      hashedPassword = bcrypt.hashSync(password, 10)

      try{
          await User.updateOne({_id:userId},{password: hashedPassword}) 
          .then( doc => {
            return response.status(204).send();
          }).catch(error => {
            return response.status(400).send(error);
          });
      } catch(error){
        return response.status(404).send({
          errmsg: error.message
        })
      }
    } else {
      return response.status(400).send({
        errmsg: "Old password is not correct"
      })
    }
    
  } else {
    return response.status(400).send({
      errmsg: "No such user."
    })
  }
}
