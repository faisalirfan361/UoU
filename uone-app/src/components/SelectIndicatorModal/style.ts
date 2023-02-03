import { makeStyles } from "@material-ui/core/styles";

export default makeStyles((theme) => ({
  inputContainer: {
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(3),
    alignItems:'center'
  },
  typoText: {
    fontSize: '10.5pt',
    color: theme.palette.common.black,
  },
  popupContainer: {
    minHeight: 580,
    paddingLeft: theme.spacing(3),
    paddingRight: theme.spacing(3),
    paddingBottom: theme.spacing(3),
  },
  searchWrapper: {
    padding: '2px 4px',
    display: 'flex',
    alignItems: 'center',
    width: "max-content",
    border:"none",
  },
  searchIcon: {
    height: theme.spacing(3),
    width: theme.spacing(3),
    color: theme.common.grey[500],
    fontWeight: "bold",
  },
  searchInput: {
    marginLeft: theme.spacing(1),
    flex: 1,
    color: theme.common.grey[600],
    fontSize: 18
  },
  newIndicatorContainer: {
    marginTop: theme.spacing(3),
    marginBottom: theme.spacing(3),
  },
  kpiNameContainer: {
    justifyItems: "center",
  },
  kpiBackgroundItem: {
    backgroundColor: theme.common.grey[100],
    borderTop: `1px solid ${theme.common.grey[300]}`,
    borderBottom: `1px solid ${theme.common.grey[300]}`,
  },
  kpiItem: {
    backgroundColor: theme.palette.common.white,
  },
  Typoselect: {
    color: theme.palette.common.black,
    fontSize: 16
  },
  radioCheckIcon: {
    fontSize: 30,
    color: theme.common.uoneLightBlue[600],
  },
  radioIcon: {
    fontSize:30
  },
  typoKpiListHeading: {
    fontSize: 11,
    color: theme.common.grey[900]
  },
  btnCancel: {
    marginRight: theme.spacing(2),
  }
}));
