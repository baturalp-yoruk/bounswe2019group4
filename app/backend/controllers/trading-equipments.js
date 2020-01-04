const {findUserComments} = require('../utils')

/*
  Get method for information of specific trading equipment.
*/
module.exports.getTradingEquipment = async (request, response) => {
  let TradingEquipment = request.models['TradingEquipment']
  let TradingEqFollow = request.models['TradingEquipmentFollow']
  let CurrentTradingEquipment = request.models['CurrentTradingEquipment']
  let Comment = request.models['Comment']
  let TradingEq = request.params['code'].toUpperCase()
  let following = false
  let yourPrediction = "noVote"

  let TradingEqPrediction = request.models['TradingEquipmentPrediction']
  var currentDay = new Date();
  var day_format = currentDay.toISOString().slice(0,10); // yyyy-mm-dd
  // If user is logged in, control whether user follows that equipment or not.
  if(request.session['user']){
    UserId = request.session['user']._id
    result = await TradingEqFollow.findOne({UserId , TradingEq})
    row = await TradingEqPrediction.findOne({UserId, TradingEq, Date: day_format})
    if(row){
      yourPrediction = row.Prediction
    }

    if(result){
      following = true
    }
  }
  current = await CurrentTradingEquipment.findOne({ from : TradingEq});

  comments = await findUserComments({ related : TradingEq, about : "TRADING-EQUIPMENT"})

  // Returns all values of given currency
  values = await TradingEquipment.find({ code: TradingEq }).sort({ Date: -1})

  ups = await TradingEqPrediction.find({TradingEq, Date: day_format}).where('Prediction').equals('up')
  downs = await TradingEqPrediction.find({TradingEq, Date: day_format}).where('Prediction').equals('down')
  numberOfUps = ups.length
  numberOfDowns = downs.length

  if(values.length == 0){
    return response.status(400).send({
      errmsg: "No such currency."
    })
  }

  return response.send({
    yourPrediction,
    following,
    current,
    comments,
    values,
    numberOfUps,
    numberOfDowns
  }); 
}

/*
  Get method for information of specific trading equipment.
*/
module.exports.getCurrentValues = async (request, response) => {
  let CurrentTradingEquipment = request.models['CurrentTradingEquipment']

  // Returns current values of all currency
  currencies = await CurrentTradingEquipment.find()

  return response.send({
    currencies
  }); 
}

/*
  Post method for following specific trading equipment.
*/
module.exports.followTradingEq = async (request, response) => {
  let TradingEqFollow = request.models['TradingEquipmentFollow']

  const UserId = request.session['user']._id
  const TradingEq = request.query.tEq.toUpperCase()

  try{
    row = await TradingEqFollow.findOne({ UserId, TradingEq })

    if (!row) {  // If no instance is returned, credentials are invalid
      let follow = new TradingEqFollow({
        UserId: UserId,
        TradingEq: TradingEq,
      });

    follow.save()
      .then(doc => {
        return response.status(204).send();
      }).catch(error => {
        return response.status(400).send(error);
      });

    } else{
      throw Error('Already followed that currency.') // Send only the extracted keys
    }
  } catch(error){
    return response.status(404).send({
      errmsg: error.message
    })
  }
}

/*
  Post method for unfollowing specific trading equipment.
*/
module.exports.unfollowTradingEq = async (request, response) => {
  let TradingEqFollow = request.models['TradingEquipmentFollow']

  const UserId = request.session['user']._id
  const TradingEq = request.query.tEq.toUpperCase()
  
  TradingEqFollow.deleteOne({ UserId, TradingEq }, (err, results) => {
    if(err){
      return response.status(404).send({
        errmsg: "Failed."
      })
    }

    return response.sendStatus(204);
  });
}


/*
  Post method for making a prediction regarding the increase or decrease of a specific trading equipment.
*/
module.exports.predictTradingEq = async (request, response) => {
  let TradingEqPrediction = request.models['TradingEquipmentPrediction']

  const UserId = request.session['user']._id
  const TradingEq = request.body["tEq"].toUpperCase();
  const value = request.body["value"];
  const prediction_value = request.body['prediction']

  var currentDay = new Date();
  var day_format = currentDay.toISOString().slice(0,10); // yyyy-mm-dd

  try{
    row = await TradingEqPrediction.findOne({ UserId, TradingEq, Date: day_format })
    if (!row) {  
      // If no instance is returned, make a new prediction
      let prediction = new TradingEqPrediction({
        _id: {UserId: UserId, TradingEq: TradingEq, Date: day_format},
        UserId: UserId,
        TradingEq: TradingEq,
        Date: day_format,
        DateWithTime: currentDay,
        Prediction: prediction_value,
        CurrentValue: value,
        Result: ""
      });

      prediction.save()
      .then(doc => {
        return response.status(204).send();
      }).catch(error => {
        return response.status(400).send(error);
      });

    } else if(row.Prediction == prediction_value) {
      TradingEqPrediction.deleteOne({ UserId, TradingEq, Date: day_format }, (err, results) => {
        if(err){
          return response.status(404).send({
            errmsg: "Failed."
          })
        }
        return response.sendStatus(204);
      })
    } else{
      // User already made a prediction for that currency. Change the prediction and update database
      TradingEqPrediction.updateOne({_id:row._id},{ Prediction: prediction_value, CurrentValue: value, value }) 
      .then(doc => {
        return response.status(204).send();
      }).catch(error => {
        return response.status(400).send(error);
      });
    }
  } catch(error){
    return response.status(404).send({
      errmsg: error.message
    })
  }
}

  /*
    Get method for list all alerts  of such user.
  */
module.exports.getAlerts = async (request, response) => {
  let TradingEquipmentAlert = request.models['TradingEquipmentAlert']
  alerts = await TradingEquipmentAlert.find({userId: request.session['user']._id})
  return response.send({alerts})
}

  /*
    Post method for setting alert for trading equipment.
  */
module.exports.setAlert = async (request, response) => {
  let TradingEquipmentAlert = request.models['TradingEquipmentAlert']
  let CurrentTradingEquipment = request.models['CurrentTradingEquipment']

  let currency = request.body.currency.toUpperCase()

  let compare = request.body.compare.toUpperCase()

  if(compare != "HIGHER" && compare != "LOWER"){
    return response.status(400).send({
      errmsg: "Compare of order must either HIGHER or LOWER"
    })
  }

  let to = 'EUR'

  if(currency == 'EUR')
    to = 'USD'

  let currentTeq = await CurrentTradingEquipment.findOne({from: currency, to: to})

  if(parseFloat(currentTeq.rate) > request.body.rate && compare == "HIGHER"){
    return response.status(400).send({
      errmsg: "Exchange rate is already higher than given rate."
    })
  }

  if(parseFloat(currentTeq.rate) < request.body.rate && compare == "LOWER"){
    return response.status(400).send({
      errmsg: "Exchange rate is already lower than given rate."
    })
  }

  let alert = new TradingEquipmentAlert({
    ...request.body,
    userId: request.session['user']._id,
    compare: compare,
    currency: currency
  })

  alert.save().then(doc => {
    return response.status(204).send()
  }).catch(error => {
    return response.status(400).send()
  })
}

  /*
    Delete method for deleting an order investment.
  */
module.exports.deleteAlert = async (request, response) => {
  let TradingEquipmentAlert = request.models['TradingEquipmentAlert']

  TradingEquipmentAlert.deleteOne({_id: request.params['id'], userId: request.session['user']._id}, (err, results) => {
    if(err){
      return response.status(404).send({
        errmsg: "Failed."
      })
    }

    return response.sendStatus(204);
  })
}
