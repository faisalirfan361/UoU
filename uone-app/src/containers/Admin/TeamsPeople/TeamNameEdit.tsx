import React, { useState } from "react";
import Box from "@material-ui/core/Box";
import { Can } from "context/Ability/Can";
import EditButton from "components/EditButton";
import useStyle from "./styles";
import TeamsNameEditIProps from "./types";
import { Typography, TextField } from "@material-ui/core";
import API from "@aws-amplify/api";
import config from "../../../config";
import { useSnackbar } from "notistack";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../../constants";

const TeamNameEdit: React.FC<TeamsNameEditIProps> = ({ user }) => {
  const classes = useStyle();
  const [editItem, setEditItem] = useState(false);
  const mask = user.mask;
  const [userName, setUserName] = useState(
    mask
      ? `${mask?.firstName} ${mask?.lastName} `
      : `${user.attributes?.firstName} ${user.attributes?.lastName} `
  );
  const { enqueueSnackbar } = useSnackbar();

  const handleNameUpdate = (key: any) => {
    if (key.keyCode == 13) {
      let firstName = userName.split(" ").slice(0, -1).join(" ");
      let lastName = userName.split(" ").slice(-1).join(" ");
      let data: any = {
        type: "user",
        entityId: user.entityId,
        clientId: user.clientId,
        cognitoIdentityId: user.cognitoIdentityId,
        departmentUOneId: user.departmentUOneId,
        departmentId: user.departmentId,
        roleId: user.roleId,
        userUOneId: user.userUOneId,
        attributes: {
          ...user.attributes,
          firstName: firstName,
          lastName: lastName,
        },
        mask: {
          ...(user.mask || {}),
          firstName: firstName,
          lastName: lastName,
        },
      };
      API.post(config.apiGateway.NAME, "/entity/update", {
        body: data,
      })
        .then(() => {
          enqueueSnackbar(
            "User Name Updated Successfully.",

            SUCCESS_TOAST_OPTIONS
          );

          setEditItem(false);
        })
        .catch(() => {
          enqueueSnackbar("Failed to Update User Name.", ERROR_TOAST_OPTIONS);
        });
    }
  };

  return (
    <>
      <Box p={0} m={1} alignSelf="center" alignItems="center" flexGrow={1}>
        {editItem ? (
          <TextField
            value={userName}
            onChange={(event) => {
              setUserName(event.target.value);
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
            {userName}
          </Typography>
        )}
      </Box>

      <Box mb={1} component="div">
        <EditButton
          disableRipple={false}
          onClick={(e: any) => setEditItem(!editItem)}
          className={classes.coinStoreButton}
        />
      </Box>
    </>
  );
};

export default TeamNameEdit;
