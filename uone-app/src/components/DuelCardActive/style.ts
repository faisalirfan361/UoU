import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  duelCardWrapper: {
    padding:`${theme.spacing(1.25)}px 0px`
  },
  duelContainer: {
    width: "100%",
    boxShadow: "0px 1px 5px rgba(0, 0, 0, 0.2)",
    borderRadius: 3
  },
  duelContent: {
    padding:`${theme.spacing(1)}px ${theme.spacing(1)}px`,
    width: '100%'
  },
  duelContentMobile: {
    padding:`${theme.spacing(6)}px ${theme.spacing(0)}px ${theme.spacing(2)}px`,
  },
  msgContainer: {
    paddingTop: theme.spacing(1),
  },
  msgHeading: {
    color: theme.common.grey[900],
    fontSize: 11,
    fontWeight: 500
  },
  msgContent: {
    color: theme.palette.common.black,
    fontSize: 14,
    fontWeight: 500,
    marginTop: theme.spacing(0.5),
    
  },
  duelFooter: {
    padding: theme.spacing(2),
    borderTop: '1px solid #CDCDCD',
  },
  btnMargin: {
    width: theme.spacing(2),
  }
}));
