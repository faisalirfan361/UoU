import { makeStyles } from "@material-ui/core/styles";

const Style = makeStyles((theme) => ({
  root: {
    marginTop: theme.spacing(2),
  },
  avatar: {
    width: 54,
    height: 54,
    padding: 2,
    borderRadius: "50%",
    background: "#252525",
    border: "1px solid #252525",
    backgroundClip: "content-box",
    marginTop: -6,
  },
  subtitle1: {
    fontStyle: "normal",
    fontWeight: 500,
    fontSize: 16,
    color: "#171717",
    textAlign: "left",
  },
  subtitle2: {
    fontStyle: "normal",
    fontWeight: 500,
    fontSize: 14,
    color: "#0065F2",
    textAlign: "right",
  },
  tableHeaderContainer: {
    padding: '10px 0px'
  },
  tableHeader: {
    textAlign: 'center',
    fontWeight: 500,
  },
  tableRow: {
    height: '71px',
    backgroundColor: 'transparent',
    borderTop: '1px solid #D1D7DA',
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    textAlign: 'center',

    '&:last-child': {
      borderBottom: '1px solid #D1D7DA',
    }
  },
  roleName: {
    textAlign: "left",
    paddingLeft: "10px"
  },
  imageContainer: {
    borderLeft: '1px solid #D1D7DA',
    height: '71px',
    lineHeight: '71px',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',

    '&:last-child': {
      borderRight: '1px solid #D1D7DA',
    }
  },
  iconContainer: {
    borderLeft: '1px solid #D1D7DA',
    height: '71px',
    lineHeight: '71px',
    cursor: 'pointer',

    '&:last-child': {
      borderRight: '1px solid #D1D7DA',
    }
  },
  onRoleIcon: {
    fontSize: '30px',
    color: '#2FB0D9',
    height: '71px'
  },
  offRoleIcon: {
    fontSize: '30px',
    color: '#8B9BA3',
    height: '71px'
  }
}));

export default Style;
