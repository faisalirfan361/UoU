import axios from "axios";

/*
Temporary object to mock data that is being use in the leaderboard display.
Should be removed after leaderboard endpoint is created
-Aramis 06/25/2021
 */

const mockUsers = {
  points: -1,
  getUsers: async function(count: number) {

    if(count == 0){
      this.points = Math.random()* (12001 - 500) + 500;
    }


    if(count <90){
      const data = await axios.get('https://api.randomuser.me/?results=30')
        .then((resp) => resp.data);

      const users = data.results.map((user: any, index:number)=>{
        // @ts-ignore
        return this.mapUser(user, index+1);
      });

      return {
        response: users,
      };
    }else{
      return {
        response: [],
      };
    }
  },
  getPerformance: function(){
    const random = Math.round(Math.random() * (100 - 1) + 1);
    const performance = random % 2 === 0? 'Up': 'Down';

    return performance;
  },
  mapUser: function(user: any, id: number)  {
    return {
      id: id,
      firstName : user.name.first,
      lastName: user.name.last,
      profileImg: user.picture.thumbnail,
      points: this.getPoints().toLocaleString(),
      performance: this.getPerformance(),
    };
  },
  getPoints: function(){
    const diff = Math.random()* (11 - 1) + 1;

    if(this.points - diff <0){
      return 0;
    }else{
      this.points -=diff;
      return Math.round(this.points);
    }
  }
}


export default mockUsers;
