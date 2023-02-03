import {
  Theme,
  Box,
  Grid,
  makeStyles,
  Typography,
  createStyles,
  Chip,
  IconButton,
  Menu,
  MenuItem,
} from "@material-ui/core";
import React, { FC, useState, useContext } from "react";
import MoreVertIcon from "@material-ui/icons/MoreHoriz";

import useGameCardLayoutStyles from "./style";
import { humanReadableDecimal } from "../../../utils/humanReadable";
import { AbilityContext, Can } from "context/Ability/Can";
import { useAbility } from "@casl/react";
import { AnyAbility } from "@casl/ability";

export interface GameCardHeaderProps {
  title: string;
  status: number;
  coins?: number | string;
  onEdit?(): void;
  onDelete?(): void;
}

interface statusType {
  text: string;
  color: string;
}

const statusContainer: statusType[] = [
  {
    text: "New!",
    color: "#F25BA4",
  },
  {
    text: "Active!",
    color: "#5AD787",
  },
  {
    text: "Draw!",
    color: "#5D7793",
  },
  {
    text: "Winner!",
    color: "#2FB0D9",
  },
];

const GameCardHeader: FC<GameCardHeaderProps> = ({
  title,
  status,
  coins,
  onDelete,
  onEdit,
}) => {
  const styles = useGameCardLayoutStyles();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const ability = useContext(AbilityContext);

  const handleOpenMenu = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleDelete = () => {
    if (onDelete) onDelete();
    setAnchorEl(null);
  };

  const handleEdit = () => {
    if (onEdit) onEdit();
    setAnchorEl(null);
  };

  return (
    <Box padding={2}>
      <Grid container spacing={1}>
        <Grid item xs style={{ minWidth: 0 }}>
          <Typography className={styles.heading}>{title}</Typography>
        </Grid>
        {ability && (
          <Grid item className={styles.coinsWrapper}>
            <IconButton
              size="small"
              aria-label="Actions"
              className={styles.actionMenuBtn}
              onClick={handleOpenMenu}
              disabled={
                !(
                  ability.can("edit", "challenges") ||
                  ability.can("delete", "challenges")
                )
              }
            >
              <MoreVertIcon />
            </IconButton>
            <Menu
              id="simple-menu"
              anchorEl={anchorEl}
              keepMounted
              open={Boolean(anchorEl)}
              onClose={handleClose}
            >
              <Can I="edit" a="challenges">
                <MenuItem dense onClick={handleEdit}>
                  EDIT
                </MenuItem>
              </Can>
              <Can I="delete" a="challenges">
                <MenuItem
                  dense
                  onClick={handleDelete}
                  className={styles.deleteBtn}
                >
                  DELETE
                </MenuItem>
              </Can>
            </Menu>
          </Grid>
        )}
      </Grid>
      <Grid container spacing={1}>
        <Grid item xs>
          <Chip
            size="small"
            label={statusContainer[status].text}
            className={styles.challengeTextColor}
            style={{ backgroundColor: `${statusContainer[status].color}` }}
          />
        </Grid>
        <Grid item className={styles.coinsWrapper}>
          <Typography className={styles.coins}>
            {humanReadableDecimal(`${coins}`)} Coins
          </Typography>
        </Grid>
      </Grid>
    </Box>
  );
};
export default GameCardHeader;
