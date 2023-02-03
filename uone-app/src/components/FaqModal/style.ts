import { makeStyles } from "@material-ui/core/styles";

export const headings = makeStyles((theme) => ({
  h3: {
    fontWeight: "normal",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  h4: {
    fontWeight: "bold",
  },
  h5: {
    color: theme.common.uoneLightBlue[700],
  },
  gutterBottom: {
    marginBottom: theme.spacing(2),
  },
}));

export const headingIcon = makeStyles((theme) => ({
  root: {
    marginRight: theme.spacing(1),
  },
  colorPrimary: {
    color: theme.common.uoneLightBlue[500],
  },
}));

export const levelTableStyles = makeStyles((theme) => ({
  levelTableContainer: {
    marginTop: theme.spacing(1)
  },
  tableHead: {
    paddingLeft: theme.spacing(2),
    display: 'flex',
    paddingBottom: theme.spacing(1),
    borderBottom: '2px solid #616161'
  },
  levelTableRowTwo: {
    display: 'flex',
    alignItems: 'center',
    paddingLeft: theme.spacing(2),
    paddingTop: theme.spacing(2),
    paddingBottom: theme.spacing(2),
    height: theme.spacing(10)
  },
  levelTableAvatarContainer: {
    display: "flex",
    alignItems: "center",
  },
  levelTableListHeading: {
    color: theme.common.uoneLightBlue[700],
    fontSize: 12,
    fontWeight: 500,
    maxWidth: theme.spacing(20),
    overflow: "hidden",
    textOverflow: "ellipsis",
    marginRight: 20,
  },
  levelTableListName: {
    fontSize: 12,
    fontWeight: 500,
    paddingLeft: theme.spacing(2),
    marginRight: theme.spacing(2),
  },
  singleListName: {
    fontSize: 12,
    fontWeight: 500,
    maxWidth: theme.spacing(20)
  }
}));
