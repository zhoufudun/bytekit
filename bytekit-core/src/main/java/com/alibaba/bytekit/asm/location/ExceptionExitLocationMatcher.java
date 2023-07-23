package com.alibaba.bytekit.asm.location;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.deps.org.objectweb.asm.Type;
import com.alibaba.deps.org.objectweb.asm.tree.LabelNode;
import com.alibaba.bytekit.asm.MethodProcessor;
import com.alibaba.bytekit.asm.TryCatchBlock;
import com.alibaba.bytekit.asm.location.Location.ExceptionExitLocation;
import com.alibaba.bytekit.asm.location.filter.LocationFilter;

public class ExceptionExitLocationMatcher implements LocationMatcher {

    private String exception;

    public ExceptionExitLocationMatcher() {
        this(Type.getType(Throwable.class).getInternalName());
    }

    public ExceptionExitLocationMatcher(String exception) {
        this.exception = exception;
    }

    @Override
    public List<Location> match(MethodProcessor methodProcessor) {
        List<Location> locations = new ArrayList<Location>();
        TryCatchBlock tryCatchBlock = methodProcessor.initTryCatchBlock(exception);

        LabelNode endLabelNode = tryCatchBlock.getEndLabelNode(); // catch 结束位置的 Lable

        LocationFilter locationFilter = methodProcessor.getLocationFilter();
        if (locationFilter.allow(endLabelNode, LocationType.EXCEPTION_EXIT, false)) {
            locations.add(new ExceptionExitLocation(tryCatchBlock.getEndLabelNode()));
        }
        return locations;
    }

}
