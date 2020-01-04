const express = require('express');
const router = express.Router();
const eventController = require('../controllers/events')
const {modelBinder, multipleModelBinder} = require('../controllers/db')
const { Event } = require('../models/event')
const { Comment } = require('../models/comment')

/*
  Get endpoint for events page.
  Check controller function for more detail
*/
router.get('/', modelBinder(Event, 'Event'), eventController.getEvents)

/*
  Get endpoint for specific event page.
  Check controller function for more detail
*/
router.get('/:id', multipleModelBinder([
  [Event, 'Event'],
  [Comment, 'Comment'],
]), eventController.getEvent)

module.exports = router
