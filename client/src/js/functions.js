export function getDateFormat(date) {
  const dateParsed = date.slice(0, date.length - 3);
  const time = new Date(dateParsed);

  let today = dateParsed.slice(4, 6).replace(/[\s.,%]/g, '');
  
  let hours = time.getHours();
  if (date.includes("PM")) {
    hours = hours + 12;
  } else {
    if (parseInt(hours) < 10) {
      hours = '0' + hours;
    }
  }

  let minutes = time.getMinutes();
  minutes = minutes< 10 ? ('0' + minutes) : minutes;

  let month;

  switch(dateParsed.slice(0, 3)) {
    case "Jan":
      month = "Января";
      break;
    case "Feb":
      month = "Февраля";
      break;
    case "Mar":
      month = "Марта";
      break;
    case "Apr":
      month = "Апреля";
      break;
    case "May":
      month = "Мая";
      break;
    case "Jun":
      month = "Июня";
      break;
    case "Jul":
      month = "Июля";
      break;
    case "Aug":
      month = "Августа";
      break;
    case "Sep":
      month = "Сентября";
      break;
    case "Oct":
      month = "Октября";
      break;
    case "Nov":
      month = "Ноября";
      break;
    case "Dec":
      month = "Декабря";
      break;
    default:
      return "";
  }

  return (today + " " + month + " " + time.getFullYear() + " "
    + hours + ":" + minutes);
}

export function getCatText(category) {
  let catText = "Категории";

  switch(category) {
    case 1:
      catText = "It";
      break;
    case 2:
      catText = "Игры";
      break;
    case 3:
      catText = "Кино";
      break;
    case 4:
      catText = "Арты";
      break;
    case 5:
      catText = "Юмор";
      break;
    case 6:
      catText = "Наука";
      break;
    case 7:
      catText = "Музыка";
      break;
    case 8:
      catText = "Новости";
      break;
    default:
      console.log("Unknown category");
  }

  return catText;
}

export function getCatId(category) {
  let catId = 0;

  switch(category) {
    case "It":
      catId = 1;
      break;
    case "Игры":
      catId = 2;
      break;
    case "Кино":
      catId = 3;
      break;
    case "Арты":
      catId = 4;
      break;
    case "Юмор":
      catId = 5;
      break;
    case "Наука":
      catId = 6;
      break;
    case "Музыка":
      catId = 7;
      break;
    case "Новости":
      catId = 8;
      break;
    default:
      console.log("Unknown category");
  }

  return catId;
}
