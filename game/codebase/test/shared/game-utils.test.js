 
describe('Game Utils', () => {
 

    const {GameUtils} = require('../../src/shared/game-utils');
    const { GameConstants } = require("../../src/shared/game-constants");

    it('should check daily schedule for same day', () => {
      let d = new Date();
      let day = d.getDate() < 10 ? "0"+d.getDate() : d.getDate();
      let dayEnd = d.getDate() < 10 ? "0"+d.getDate() : d.getDate();
      let month = (d.getMonth()+1) < 10 ? "0"+(d.getMonth()+1) : (d.getMonth()+1);
      let year = d.getFullYear();
      let hour = d.getHours() < 10 ? "0"+d.getHours() : d.getHours();
      let hourEnd = d.getHours() < 10 ? "0"+d.getHours() : d.getHours();
      let game = {
        "end_date": "" + year + "-" + month + "-" + dayEnd + "T" + hourEnd + ":30:00.000",
        "schedule": "DAILY",
        "hourEnd": hourEnd,
        "start_date": "" + year + "-" + month + "-" + day + "T" + hour + ":00:32.305"
      }
      console.log("game ", game)
      let expected = "cron(30 " + d.getHours() + " " + d.getDate() + " " + (d.getMonth()+1) + " ? " + year + ")";
      let schedule = GameUtils.generateGameSchedule(game, GameConstants)
      expect(expected).toEqual(schedule)
    })

    it('should check daily schedule for next day', () => {
      let d = new Date();
      let day = d.getDate() < 10 ? "0"+d.getDate() : d.getDate();
      let dayEnd = (d.getDate()+1) < 10 ? "0"+(d.getDate()+1) : (d.getDate()+1);
      let month = (d.getMonth()+1) < 10 ? "0"+(d.getMonth()+1) : (d.getMonth()+1);
      let year = d.getFullYear();
      let hour = (d.getHours()+6) < 10 ? "0"+(d.getHours()+6) : (d.getHours()+6);
      let hourEnd = (d.getHours() + 7) < 10 ? "0"+(d.getHours()+7) : (d.getHours()+7);
      let game = {
        "end_date": "" + year + "-" + month + "-" + dayEnd + "T" + hourEnd + ":00:00.000Z",
        "schedule": "DAILY",
        "start_date": "" + year + "-" + month + "-" + day + "T" + hour + ":30:32.305Z"
      }
      let expected = "cron(30 " + d.getHours() + " " + (d.getDate()+1) + " " + (d.getMonth()+1) + " ? " + year + ")";
      let schedule = GameUtils.generateGameSchedule(game, GameConstants)
      expect(expected).toEqual(schedule)
    })

    it('should check daily schedule for next day', () => {
      let d = new Date();
      let day = d.getDate() < 10 ? "0"+d.getDate() : d.getDate();
      let month = (d.getMonth()+1) < 10 ? "0"+(d.getMonth()+1) : ( d.getMonth()+1);
      let year = d.getFullYear();
      let hour = (d.getHours()+6) < 10 ? "0"+(d.getHours()+6) : (d.getHours()+6);
      let hourEnd = (d.getHours() - 1) < 10 ? "0"+(d.getHours()-1) : (d.getHours()-1);
      let game = {
        "end_date": "" + year + "-" + month + "-" + day + "T" + hour + ":00:00.000Z",
        "schedule": "DAILY",
        "start_date": "" + year + "-" + month + "-" + day + "T" + hourEnd + ":30:32.305Z"
      }
      let expected = "cron(0 " + d.getHours() + " " + d.getDate() + " " + (d.getMonth()+1) + " ? " + year + ")";
      let schedule = GameUtils.generateGameSchedule(game, GameConstants)
      expect(expected).toEqual(schedule)
    })

})