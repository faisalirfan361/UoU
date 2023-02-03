import React, { memo, useState } from "react";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import Avatar from "components/Avatar";
import { useSnackbar } from "notistack";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";
import { API } from "aws-amplify";

import CheckCircleRoundedIcon from "@material-ui/icons/CheckCircleRounded";
import HighlightOffRoundedIcon from "@material-ui/icons/HighlightOffRounded";
import useWebSocket from "react-use-websocket";

import useStyles from "./style";
import config from "config";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../../constants";
import StyledAvatar from "components/StyledAvatar";

const DashboardPendingApprovalsContainer = () => {
  const classes = useStyles();
  const { enqueueSnackbar } = useSnackbar();
  const { userId, jwtToken } = useRecoilValue(userAtom);
  const [approvals, setApprovals] = useState<any>([]);
  const { clientId } = useRecoilValue(userAtom);
  useWebSocket(config.webSocket + "?Authorizer=" + jwtToken, {
    onOpen: () => console.log("opened"),
    onMessage: (event) => {
      const message = JSON.parse(event.data);
      if (message) {
        if (message["teamApprovals"] && message["teamApprovals"].length) {
          let tempApprovals: any[] = [];
          message["teamApprovals"].forEach((approval: any) => {
            console.log(approval);
            tempApprovals.push({
              photoUrl: approval.imageUrl,
              username: approval.userName,
              uuid: approval.uuid,
              clientId: clientId,
              userId: approval.userId,
              imageType: approval.imageType,
              departmentId: approval.departmentId,
            });
          });
          setApprovals(tempApprovals);
        }
      }
    },
    shouldReconnect: (closeEvent) => true,
  });

  const handleDeny = (approval: any) => {
    API.post(
      config.apiGateway.NAME,
      "entity/banner-approval" + approval.userId,
      {
        body: {
          didApprove: false,
          approverUserId: userId,
          uploaderUserId: approval.userId,
          bannerImageS3URL: approval.base64ImageFile,
        },
      }
    )
      .then(() => {
        let tempApprovals = approvals.filter(function (obj: any) {
          return obj.uuid !== approval.uuid;
        });
        setApprovals(tempApprovals);
        enqueueSnackbar("Image denied successfully.", SUCCESS_TOAST_OPTIONS);
      })
      .catch(() => {
        enqueueSnackbar("Failed to approve Image.", ERROR_TOAST_OPTIONS);
      });
  };

  const handleApprove = (approval: any) => {
    API.post(config.apiGateway.NAME, "entity/banner-approval", {
      body: {
        didApprove: true,
        approverUserId: userId,
        uploaderUserId: approval.userId,
        bannerImageS3URL: approval.base64ImageFile,
      },
    })
      .then(() => {
        let tempApprovals = approvals.filter(function (obj: any) {
          return obj.uuid !== approval.uuid;
        });
        setApprovals(tempApprovals);
        enqueueSnackbar("Image approved successfully.", SUCCESS_TOAST_OPTIONS);
      })
      .catch(() => {
        enqueueSnackbar("Failed to approve Image.", ERROR_TOAST_OPTIONS);
      });
  };

  return (
    <>
      <Grid className={classes.root} container spacing={2}>
        <Grid item xs={12} sm={6}>
          <Typography
            className={classes.subtitle1}
            gutterBottom
            variant="subtitle1"
          >
            Pending Approvals
          </Typography>
        </Grid>
      </Grid>
      <Grid container spacing={2}>
        <Grid
          container
          direction="row"
          className={classes.tableHeaderContainer}
        >
          <Grid item xs={8}></Grid>
          <Grid item xs={2} className={classes.tableHeader}>
            Approve
          </Grid>
          <Grid item xs={2} className={classes.tableHeader}>
            Deny
          </Grid>
        </Grid>
        {approvals.map((approval: any, index: any) => (
          <Grid
            key={index}
            container
            direction="row"
            className={classes.tableRow}
            style={{ backgroundColor: index % 2 === 0 ? "white" : "inherit" }}
          >
            <Grid item xs={4} className={classes.roleName}>
              {approval.username}
            </Grid>
            <Grid item xs={4} className={classes.imageContainer}>
              <StyledAvatar src={approval.photoUrl} />
            </Grid>
            <Grid item xs={2} className={classes.iconContainer}>
              <div onClick={() => handleApprove(approval)}>
                <CheckCircleRoundedIcon className={classes.onRoleIcon} />
              </div>
            </Grid>
            <Grid item xs={2} className={classes.iconContainer}>
              <div onClick={() => handleDeny(approval)}>
                <HighlightOffRoundedIcon className={classes.offRoleIcon} />
              </div>
            </Grid>
          </Grid>
        ))}
        {approvals.length === 0 && (
          <Grid
            item
            xs={12}
            className={classes.iconContainer}
            style={{ backgroundColor: "white" }}
          >
            No Pending approvals.
          </Grid>
        )}
      </Grid>
    </>
  );
};

export default memo(DashboardPendingApprovalsContainer);
