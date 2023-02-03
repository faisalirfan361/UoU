import { format } from "date-fns";

/**
 * Gives you a mysql standard formatted datetime: 2018-08-08 23:00:00
 */
export const  getStandardFormattedDateTime = (date: Date = new Date()) =>
  format(date, "yyyy-MM-dd HH:mm:ss");



export const  getLongFormattedDateTime = (date: Date = new Date()) =>{
  const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July',
    'August', 'September', 'October', 'November', 'December'];
  let hours = date.getHours();
  hours = (hours + 24) % 24;
  let mid='am';
  if(hours==0){
    hours=12;
  }
  else if(hours==12)
  {
    mid='pm';
  }
  else if(hours>12)
  {
    hours=hours%12;
    mid='pm';
  }

  const minutes = date.getMinutes().toString().length ===1?`0${date.getMinutes()}`:date.getMinutes();

  return `${MONTHS[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()} -
  ${hours}:${minutes}${mid}`;

};



