import React, { useState } from "react";
import Box from "@material-ui/core/Box";
import EditButton from "components/EditButton";
import useStyle from "./styles";
import DeptNameEditIProps from "./types";
import { Typography, TextField, Grid } from "@material-ui/core";
import API from "@aws-amplify/api";
import config from "../../config";
import { useSnackbar } from "notistack";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../constants";
const DeptNameEdit: React.FC<DeptNameEditIProps> = ({ department }) => {
  const classes = useStyle();
  const [editItem, setEditItem] = useState(false);
  const [deptName, setDeptName] = useState(
    department?.mask?.dname
      ? department?.mask?.dname
      : department?.attributes?.dname
  );
  const { enqueueSnackbar } = useSnackbar();

  const handleNameUpdate = (key: any) => {
    if (key.keyCode == 13) {
      let data: any = {
        type: department.type,
        subType: department.subType,
        entityId: department.entityId,
        uoneId: department.uoneId,
        clientId: department.clientId,
        attributes: {
          ...department.attributes,
          dname: deptName,
        },
        mask: {
          ...(department.mask || {}),
          dname: deptName,
        },
      };

      API.post(config.apiGateway.NAME, "/entity/update", {
        body: data,
      })
        .then(() => {
          enqueueSnackbar(
            "Department Name Updated Successfully.",

            SUCCESS_TOAST_OPTIONS
          );
          setEditItem(false);
        })
        .catch(() => {
          enqueueSnackbar(
            "Failed to Update Department Name.",
            ERROR_TOAST_OPTIONS
          );
        });
    }
  };

  return (
    <>
      <Grid item xs={12}>
        <Grid
          container
          direction="row"
          justifyContent="space-between"
          alignItems="flex-start"
        >
          <Grid item xs={11}>
            {editItem ? (
              <TextField
                value={deptName}
                onChange={(event) => {
                  setDeptName(event.target.value);
                }}
                onKeyDown={(e) => {
                  handleNameUpdate(e);
                }}
                InputProps={{
                  disableUnderline: false,
                  className: classes.tableBodyTitle,
                }}
              />
            ) : (
              <Typography
                className={classes.tableBodyTitle}
                variant="subtitle2"
                gutterBottom
              >
                {deptName}
              </Typography>
            )}
          </Grid>
          <Grid item xs={1}>
            <EditButton
              disableRipple={false}
              onClick={(e: any) => setEditItem(!editItem)}
              className={classes.editButton}
            />
          </Grid>
        </Grid>
      </Grid>
    </>
  );
};

export default DeptNameEdit;
