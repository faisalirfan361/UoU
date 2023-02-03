import React, { useEffect, useMemo, useState, useCallback } from "react";
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import TextField from "@material-ui/core/TextField";
import config from "../../config";
import { userAtom } from "state";
import { useRecoilValue } from "recoil";
import { API } from "aws-amplify";
import { useSnackbar } from "notistack";
import Grid from "@material-ui/core/Grid";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../constants";

import CreateCoinStoreItemModalProps from "./types";
import {
  Box,
  Button,
  Dialog,
  useTheme,
  Typography,
  Tooltip,
} from "@material-ui/core";
import useCreateCoinStoreModalStyle from "./style";
import { ButtonActionComponent, ButtonDeleteItem } from "components";
import {
  UOneDialogActions,
  UOneDialogContent,
  UOneDialogTitle,
} from "components/UOneDialog";
import useSWR from "swr";
import { useDropzone } from "react-dropzone";
import { FaCloudUploadAlt } from "react-icons/fa";

const acceptedTypes = ["image/png", "image/jpg", "image/jpeg"];

const getSelfProviderId = (providersData: any, clientId: any) => {
  let selfProviderId = "";
  if (providersData) {
    providersData.forEach((item: any) => {
      if (clientId.includes(item?._name?.toLowerCase())) {
        selfProviderId = item._id;
      }
    });
  }
  return selfProviderId;
};

const CreateCoinStoreItemModal: React.FC<CreateCoinStoreItemModalProps> = ({
  open,
  onClose,
  selectedItem,
}) => {
  const theme = useTheme();
  const classes = useCreateCoinStoreModalStyle();
  const [buttonStatus, setButtonStatus] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const { clientId } = useRecoilValue(userAtom);
  const [itemTitle, setItemTitle] = useState("");
  const [itemDescription, setItemDescription] = useState("");
  const [itemValue, setItemValue] = useState(0);
  const [itemLimit, setItemLimit] = useState(0);
  const [itemId, setItemId] = useState(-1);
  const { enqueueSnackbar } = useSnackbar();
  const { data: providersData } = useSWR(
    `/raas/clients/${clientId}/raas-providers`
  );
  const provider = useMemo(
    () => getSelfProviderId(providersData, clientId),
    [providersData, clientId]
  );
  let file: any = "";

  useEffect(() => {
    if (selectedItem) {
      setItemTitle(selectedItem.title);
      setItemDescription(selectedItem.description);
      setItemValue(selectedItem.points);
      setItemLimit(selectedItem.limit ? selectedItem.limit : 0);
      setItemId(selectedItem.id);
    } else {
      setItemTitle("");
      setItemDescription("");
      setItemValue(0);
      setItemLimit(0);
    }
  }, [selectedItem]);

  const isValidFileType = (fileType: string): boolean => {
    return acceptedTypes.includes(fileType);
  };

  const getBase64 = (file: any, cb: any) => {
    let reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = function () {
      cb(reader.result);
    };
    reader.onerror = function (error) {
      console.log("Error: ", error);
    };
  };

  const handleUpdateTitle = (event: React.ChangeEvent<HTMLInputElement>) => {
    setItemTitle(event.target.value);
  };

  const handleUpdateDescription = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setItemDescription(event.target.value);
  };

  const handleUpdateValue = (event: React.ChangeEvent<HTMLInputElement>) => {
    setItemValue(parseInt(event.target.value, 10));
  };

  const handleUpdateLimit = (event: React.ChangeEvent<HTMLInputElement>) => {
    setItemLimit(parseInt(event.target.value, 10));
  };

  const handleDeleteItemClick = async () => {
    let path = `/raas/delete/ItemCard/${itemId}`;

    API.get(config.apiGateway.NAME, path, {})
      .then(() => {
        enqueueSnackbar("Deleted Item successfully.", SUCCESS_TOAST_OPTIONS);
        onClose();
        setButtonStatus(false);
      })
      .catch(() => {
        enqueueSnackbar("Failed to Delete Item.", ERROR_TOAST_OPTIONS);
        setButtonStatus(false);
      });
  };

  const handleEditItemClick = async () => {
    file = selectedFile;
    if (file && !isValidFileType(file.type)) {
      alert("Only images are allowed (png or jpg)");
      return;
    }

    let path = `/raas/clients/${clientId}/raas-providers/${provider}/items/update`;
    if (file) {
      await getBase64(file, function (resp: any) {
        let data: any = {
          file: resp,
          item_id: itemId,
          fileName: file.name,
          mime: file.type,
          title: itemTitle,
          description: itemDescription,
          value: itemValue,
        };
        setSelectedFile(null);
        API.put(config.apiGateway.NAME, path, {
          body: data,
        })
          .then(() => {
            enqueueSnackbar(
              "Updated Item successfully.",
              SUCCESS_TOAST_OPTIONS
            );
            onClose();
            setButtonStatus(false);
          })
          .catch(() => {
            enqueueSnackbar("Failed to Update Item.", ERROR_TOAST_OPTIONS);
            setButtonStatus(false);
          });
      });
    } else {
      let data: any = {
        item_id: itemId,
        title: itemTitle,
        description: itemDescription,
        value: itemValue,
      };
      setSelectedFile(null);
      API.put(config.apiGateway.NAME, path, {
        body: data,
      })
        .then(() => {
          enqueueSnackbar("Updated Item successfully.", SUCCESS_TOAST_OPTIONS);
          onClose();
          setButtonStatus(false);
        })
        .catch(() => {
          enqueueSnackbar("Failed to Update Item.", ERROR_TOAST_OPTIONS);
          setButtonStatus(false);
        });
    }
  };

  const handleCreateItemClick = async () => {
    setButtonStatus(true);
    file = selectedFile;
    if (!file) {
      alert("Please select an image to upload.");
      return;
    }
    if (!isValidFileType(file.type)) {
      alert("Only images are allowed (png or jpg)");
      return;
    }
    let path = `/raas/clients/${clientId}/raas-providers/${provider}/items/create`;
    await getBase64(file, function (resp: any) {
      let data: any = {
        file: resp,
        fileName: file.name,
        mime: file.type,
        title: itemTitle,
        description: itemDescription,
        value: itemValue,
      };
      API.post(config.apiGateway.NAME, path, {
        body: data,
      })
        .then(() => {
          enqueueSnackbar("Created Item successfully.", SUCCESS_TOAST_OPTIONS);
          onClose();
          setButtonStatus(false);
        })
        .catch(() => {
          enqueueSnackbar("Failed to Created Item.", ERROR_TOAST_OPTIONS);
          setButtonStatus(false);
        });
    });
  };
  const onDrop = (acceptedFiles: any) => {
    setSelectedFile(acceptedFiles[0]);
  };
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: acceptedTypes,
    multiple: false,
    onDrop,
  });
  return (
    <div>
      <Dialog open={open} onClose={onClose} maxWidth={"md"}>
        <UOneDialogTitle id="coin-store-create-item" onClose={onClose}>
          {selectedItem ? "Edit" : "Create"} Coin Store Card
        </UOneDialogTitle>
        <UOneDialogContent dividers>
          <Card
            className={classes.itemCard}
            style={{ backgroundColor: theme.common?.grey[200] }}
          >
            <div className={classes.details}>
              <CardContent className={classes.content}>
                <Grid container direction="row" className={classes.fieldClass}>
                  <TextField
                    onChange={handleUpdateTitle}
                    value={itemTitle}
                    id="title"
                    type="title"
                    className={classes.fieldBox}
                    name="title"
                    label="title"
                    variant="outlined"
                  />
                </Grid>
                <Grid container direction="row" className={classes.fieldClass}>
                  <TextField
                    onChange={handleUpdateDescription}
                    value={itemDescription}
                    id="description"
                    className={classes.fieldBox}
                    type="description"
                    name="description"
                    label="description"
                    variant="outlined"
                  />
                </Grid>
                <Grid container direction="row" className={classes.fieldClass}>
                  <TextField
                    id="cost"
                    onChange={handleUpdateValue}
                    value={itemValue}
                    type="cost"
                    inputProps={{ type: "number" }}
                    className={classes.fieldBox}
                    name="cost"
                    label="Cost"
                    variant="outlined"
                  />
                </Grid>
                <Grid container direction="row" className={classes.fieldClass}>
                  <Tooltip
                    classes={{
                      tooltip: classes.tooltip,
                      popper: classes.popper,
                    }}
                    title={`Limit is the amount of times you wish to have this item redeemed. Once the card has reached its limit it can no longer be redeemed.`}
                    placement="bottom"
                  >
                    <TextField
                      id="limit"
                      onChange={handleUpdateLimit}
                      value={itemLimit}
                      type="number"
                      inputProps={{
                        type: "number",
                        min: 0,
                      }}
                      className={classes.fieldBox}
                      name="limit"
                      label="Limit"
                      variant="outlined"
                    />
                  </Tooltip>
                </Grid>
                <div {...getRootProps()} className={classes.dropzone}>
                  <input {...getInputProps()} />
                  <Box className={classes.dropzoneIcon}>
                    <FaCloudUploadAlt />
                  </Box>
                  <Typography variant="h4" className={classes.dropzoneTitle}>
                    Upload Photo
                  </Typography>
                  <p>Drag and drop file here...</p>
                  <p className={classes.textRequirement}>
                    Image size requirements: file size 2MB max.
                  </p>
                </div>
                {selectedFile && <div>File Upload: {selectedFile["name"]}</div>}
              </CardContent>
            </div>

            <div className={classes.redeemContainer}>
              {selectedItem ? (
                <Box component="div">
                  <Box component="div" textAlign="center" mb={1}>
                    <ButtonDeleteItem
                      handleOnClick={handleDeleteItemClick}
                      disabled={buttonStatus}
                    >
                      Delete
                    </ButtonDeleteItem>
                  </Box>
                  <Box component="div">
                    <ButtonActionComponent
                      handleOnClick={handleEditItemClick}
                      disabled={buttonStatus}
                    >
                      Edit Item
                    </ButtonActionComponent>
                  </Box>
                </Box>
              ) : (
                <Box component="div">
                  <ButtonActionComponent
                    handleOnClick={handleCreateItemClick}
                    disabled={buttonStatus}
                  >
                    Create Item
                  </ButtonActionComponent>
                </Box>
              )}
            </div>
          </Card>
        </UOneDialogContent>
        <UOneDialogActions>
          <Button
            onClick={onClose}
            color="primary"
            variant="outlined"
            size="small"
          >
            Cancel
          </Button>
        </UOneDialogActions>
      </Dialog>
    </div>
  );
};

export default CreateCoinStoreItemModal;
