/**
 * TEST E: Collapsible + Action Buttons in header
 * Like the card branch's actionButtonGroup but in AccordionSummary
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import IconButton from '@mui/material/IconButton';
import ButtonGroup from '@mui/material/ButtonGroup';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import PersonIcon from '@mui/icons-material/Person';
import { SampleChildrenVertical } from './SampleChildren';

export function CollapsibleWithActionButtons() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Test E: Collapsible + Action Buttons</Typography>

      {/* E1: Action buttons inside AccordionSummary */}
      <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
        E1: Actions inside AccordionSummary (tricky — clicking buttons toggles accordion)
      </Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Grid container direction="row" alignItems="center" justifyContent="space-between" sx={{ width: '100%', mr: 2 }}>
              <Grid>
                <Grid container direction="row" alignItems="center">
                  <PersonIcon sx={{ marginRight: 1 }} />
                  <Typography variant="h5">Group With Actions</Typography>
                </Grid>
              </Grid>
              <Grid>
                <ButtonGroup size="small">
                  <IconButton color="primary" onClick={(e) => { e.stopPropagation(); alert('Add'); }} title="Add">
                    <AddIcon />
                  </IconButton>
                  <IconButton color="primary" onClick={(e) => { e.stopPropagation(); alert('Edit'); }} title="Edit">
                    <EditIcon />
                  </IconButton>
                  <IconButton color="primary" onClick={(e) => { e.stopPropagation(); alert('Delete'); }} title="Delete">
                    <DeleteIcon />
                  </IconButton>
                </ButtonGroup>
              </Grid>
            </Grid>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* E2: Action buttons outside AccordionSummary — in AccordionActions */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>
        E2: Actions below content (MUI AccordionActions)
      </Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <PersonIcon sx={{ marginRight: 1 }} />
            <Typography variant="h5">Group With Actions Below</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', px: 2, pb: 1 }}>
            <ButtonGroup size="small">
              <IconButton color="primary" onClick={() => alert('Add')} title="Add">
                <AddIcon />
              </IconButton>
              <IconButton color="primary" onClick={() => alert('Edit')} title="Edit">
                <EditIcon />
              </IconButton>
              <IconButton color="primary" onClick={() => alert('Delete')} title="Delete">
                <DeleteIcon />
              </IconButton>
            </ButtonGroup>
          </Box>
        </Accordion>
      </Grid>

      {/* E3: Collapsible + Card + Actions (full combo) */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>
        E3: Full combo — Card wrapping Accordion with actions in summary
      </Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded elevation={1} sx={{ borderRadius: 1 }}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Grid container direction="row" alignItems="center" justifyContent="space-between" sx={{ width: '100%', mr: 2 }}>
              <Grid>
                <Grid container direction="row" alignItems="center">
                  <PersonIcon sx={{ marginRight: 1 }} />
                  <Typography variant="h5">Card-like Collapsible with Actions</Typography>
                </Grid>
              </Grid>
              <Grid>
                <ButtonGroup size="small">
                  <IconButton color="primary" onClick={(e) => { e.stopPropagation(); alert('Add'); }}>
                    <AddIcon />
                  </IconButton>
                  <IconButton color="primary" onClick={(e) => { e.stopPropagation(); alert('Edit'); }}>
                    <EditIcon />
                  </IconButton>
                </ButtonGroup>
              </Grid>
            </Grid>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>
    </Box>
  );
}
