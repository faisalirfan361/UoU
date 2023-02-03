import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  agentContainer: {
    marginTop: theme.spacing(1)
  },
  agentRowOne: {
    paddingLeft: theme.spacing(2),
    display: 'flex',
    paddingBottom: theme.spacing(1),
  },
  agentRowTwo: {
    display: 'flex',
    alignItems: 'center',
    paddingLeft: theme.spacing(2),
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    borderTop: '1px solid #CDCDCD',
    borderBottom: '1px solid #CDCDCD',
  },
  agentRowThree: {
    display: 'flex',
    alignItems: 'center',
    paddingLeft: theme.spacing(2),
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
  },
  agentAvatarContainer: {
    display: "flex",
    alignItems: "center"
  },
  agentListHeading: {
    color: theme.common.grey[900],
    fontSize: 11,
    fontWeight: 500
  },
  agentListText: {
    color: '#171717',
    fontSize: 14,
    fontWeight: 500
  },
  agentListName: {
    fontSize: 14,
    color: `${theme.common.uoneLightBlue[400]}`,
    fontWeight: 500,
    paddingLeft: theme.spacing(2),
  },
  agentListNameWhite: {
    fontSize: 14,
    color: theme.palette.common.white,
    fontWeight: 500,
    paddingLeft: theme.spacing(2),
  },
}));
