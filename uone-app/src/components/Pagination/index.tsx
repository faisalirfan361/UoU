import React from "react";
import Grid from "@material-ui/core/Grid";
import Pagination from '@material-ui/lab/Pagination';
import IProps from "./types";
import useStyle from "./style";


const PaginationComponent: React.FC<IProps> = ({
                                                 disabled,
                                                 totalPages,
                                                 onChanged,
                                               }) => {
  const styles = useStyle();

  return (
    <>
      <Grid container direction="row" className={styles.row}>
        <Pagination
          disabled={disabled}
          count={totalPages}
          onChange={onChanged}
          showFirstButton={true}
          showLastButton={true}
          color="primary"
        />
      </Grid>
    </>
  );
};

export default PaginationComponent;
