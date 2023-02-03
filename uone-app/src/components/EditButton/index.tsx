import { IconButton } from "@material-ui/core";
import { FaPencilAlt } from "react-icons/fa";

import useEditButtonStyles from "./styles";

const EditButton = (props: any) => {
  const classes = useEditButtonStyles();
  return (
    <IconButton color="primary" classes={classes} {...props}>
      <FaPencilAlt />
    </IconButton>
  );
};

export default EditButton;
