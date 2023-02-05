/**
 * GameUtils provides basic utilities for UOneGames
 */
const GameUtils = Object.freeze({
    /**
     * generateGameSchedule it generates game schedules based on types
     * DAILY< WEEKLY and MONTHLY, based on that it generates AWS Cron for gmes
     * 
     * @param UOneGame game 
     * @param GameConstants GameConstants 
     * @returns 
     */
    generateGameSchedule: function(game, GameConstants) {
        let gameDate = new Date(game.start_date); 
        let currentDate = new Date();
        let endDate = new Date(game.end_date);
        if (endDate <= currentDate)
          return -1;
        if ( GameConstants.GAME_SCHEDULE_TYPES.ONCE == game.schedule) {
          gameDate = new Date(game.end_date);
        }
        else if (GameConstants.GAME_SCHEDULE_TYPES.DAILY == game.schedule) { 
          gameDate.setDate(currentDate.getDate() + 1);
        }
        else if (GameConstants.GAME_SCHEDULE_TYPES.WEEKLY == game.schedule) { 
          gameDate.setDate(currentDate.getDate() + 7);
        }
        else if (GameConstants.GAME_SCHEDULE_TYPES.MONTHLY == game.schedule) { 
          gameDate.setDate(currentDate.getDate() + 30);
        }
        else {
          gameDate = new Date(game.end_date);
        }
        let minute = gameDate.getMinutes();
        let hour = gameDate.getHours();
        let dayOfMonth = gameDate.getDate();
        let month = gameDate.getMonth() + 1; // for monthly
        let dayOfWeek = "?"; // for weekly
        let year = gameDate.getFullYear();
        return "cron(" + minute + " " + hour + " " + dayOfMonth + " " + month + " " + dayOfWeek + " " + year + ")";
    },
    /**
     * getDayInCurrentSchedulePeriod how many days in are we in games or goals
     * 
     * @param String dateString 
     * @returns 
     */
    getDayInCurrentSchedulePeriod: function(dateString) {
      var now = new Date();
      var today = new Date(now.getYear(),now.getMonth(),now.getDate());
      var dateNow = now.getDate();
      var startDate = new Date(dateString.substring(6,10),
                         dateString.substring(0,2)-1,                   
                         dateString.substring(3,5)                  
                         );
      var diff = startDate.getDate(); 
      return (dateNow >= diff) ? dateNow - diff : 31 + dateNow - diff;
    }
})

module.exports = { GameUtils };
